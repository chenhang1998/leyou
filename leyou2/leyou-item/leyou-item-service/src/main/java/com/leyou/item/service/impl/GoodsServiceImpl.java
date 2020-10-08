package com.leyou.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import com.leyou.item.service.CategoryService;
import com.leyou.item.service.GoodsService;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 查询商品列表
     *
     * @param page
     * @param rows
     * @param key
     * @param saleable*/
    @Override
    public PageResult<SpuBo> querySpuByPage(Integer page, Integer rows, String key, Boolean saleable) {
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //添加模糊查询
        if (!StringUtils.isEmpty(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        //添加上下架过滤查询
        if (saleable!=null) {
            criteria.andEqualTo("saleable", saleable);
        }
        //添加分页查询
        PageHelper.startPage(page,rows);
        //执行查询
        List<Spu> spus = spuMapper.selectByExample(example);
        //转化spu结果集为spubo
        List<SpuBo> spuBos = spus.stream().map(spu -> {
            SpuBo spuBo = new SpuBo();
            BeanUtils.copyProperties(spu, spuBo);                //把spu属性值copy给spubo
            //查询品牌名称
            Brand brand = brandMapper.selectByPrimaryKey(spuBo.getBrandId());
            spuBo.setBname(brand.getName());
            //查询分类名称
            List<String> strings = categoryService.queryNameByIds(Arrays.asList(spuBo.getCid1(),spuBo.getCid2(),spuBo.getCid3()));
            spuBo.setCname(strings.get(0)+"/"+strings.get(1)+"/"+strings.get(2));
            return spuBo;
        }).collect(Collectors.toList());
        //返回结果集
        PageResult<SpuBo> result = new PageResult<>();
        result.setItems(spuBos);//查询的商品集
        PageInfo<Spu> pageInfo=new PageInfo<>(spus);
        result.setTotal(pageInfo.getTotal());//查询出的总条数
        return result;
    }

    /**
     * 添加商品
     * */
    @Transactional
    @Override
    public void saveGoods(SpuBo spuBo) {
        //新增spu
        spuBo.setId(null);
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        spuMapper.insertSelective(spuBo);
        //新增spuDetail
        SpuDetail spuDetail=spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        spuDetailMapper.insertSelective(spuDetail);
        //新增sku和新增stock
        spuBo.getSkus().forEach(sku -> {
            //新增sku
            sku.setId(null);
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            skuMapper.insertSelective(sku);
            //新增stock
            Stock stock=new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stock.setSeckillStock(null);
            stock.setSeckillTotal(null);
            stockMapper.insertSelective(stock);

            //rabbitmq发送insert消息
            sendMsg("insert",spuBo.getId());
        });
    }

    /**
     * 编辑商品
     * */
    @Transactional
    @Override
    public void updateGoods(SpuBo spuBo) {
        //查询要删除的sku
        Sku sku=new Sku();
        sku.setSpuId(spuBo.getId());
        List<Sku> skus = this.skuMapper.select(sku);

        //删除stock
        skus.forEach(sku1 -> {
            this.stockMapper.deleteByPrimaryKey(sku1.getId());
        });

        //删除sku
        this.skuMapper.delete(sku);

        //新增sku
        spuBo.getSkus().forEach(sku1 -> {
            //新增sku
            sku1.setId(null);
            sku1.setSpuId(spuBo.getId());
            sku1.setCreateTime(spuBo.getCreateTime());
            sku1.setLastUpdateTime(new Date());
            skuMapper.insertSelective(sku1);
            //新增sku和新增stock
            Stock stock=new Stock();
            stock.setSkuId(sku1.getId());
            stock.setStock(sku1.getStock());
            stock.setSeckillStock(null);
            stock.setSeckillTotal(null);
            stockMapper.insertSelective(stock);
        });

        //更新spu和spuDetail
        spuBo.setCreateTime(null);
        spuBo.setValid(null);
        spuBo.setSaleable(null);                                                     //防止恶意注入式更新数据
        spuBo.setLastUpdateTime(new Date());
        this.spuMapper.updateByPrimaryKeySelective(spuBo);
        this.spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

        //rabbitmq发送更新消息
        this.sendMsg("update",spuBo.getId());
    }

    /**
     * 根据spuId查询spuDetail
     * */
    @Override
    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        return spuDetailMapper.selectByPrimaryKey(spuId);
    }

    /**
     * 根据spuId查询Skus
     * */
    @Override
    public List<Sku> querySkusBySpuId(Long spuId) {
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skus = this.skuMapper.select(sku);
        skus.forEach(sku1 -> {
            Stock stock = this.stockMapper.selectByPrimaryKey(sku1.getId());
            sku1.setStock(stock.getStock());
        });
        return skus;
    }

    /**
     * 根据spuId查询Spu
     * */
    @Override
    public Spu querySpuBySpuId(Long spuId) {
        return this.spuMapper.selectByPrimaryKey(spuId);
    }

    /**
     * rabbitmq发送消息
     * */
    private void sendMsg(String s,Long spuId) {
        try {
            this.amqpTemplate.convertAndSend("item."+s,spuId);
        }catch (AmqpException e){
            e.printStackTrace();
        }
    }
}

