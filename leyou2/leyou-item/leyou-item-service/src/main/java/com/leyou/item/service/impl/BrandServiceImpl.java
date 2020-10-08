package com.leyou.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    BrandMapper brandMapper;

    /**
     * 根据查询条件分页并排序查询品牌信息
     * */
    @Override
    public PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        //初始化example对象
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();
        //根据name模糊，或者根据首字母
        if (StringUtils.isNotBlank(key)){
            criteria.andLike("name","%"+key+"%").orEqualTo("letter",key);        //添加查询条件，key为品牌名称或者key为首字母
        }
        //添加分页条件
        PageHelper.startPage(page,rows);
        //添加排序条件
        if (StringUtils.isNotBlank(sortBy)){
            example.setOrderByClause(sortBy+" "+(desc? "desc":"asc"));
        }
        List<Brand> brands = brandMapper.selectByExample(example);
        //包装成pageInfo
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        //包装成分页结果集返回
        PageResult<Brand> result = new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
        return result;
    }

    /**
     *新增品牌
     * brand
     * cids
     */
    @Transactional      //添加事务（当对数据库进行增删改操作时，数据库数据发生变动，就必须要用事务提交）
    @Override
    public void savebrand(Brand brand, List<Long> cids) {

        //添加品牌
        brandMapper.insertSelective(brand);

        //向中间表插入数据
        cids.forEach(cid ->{
            brandMapper.insertCategoryAndBrand(cid,brand.getId());
        });
    }

    /**
     * 根据商品分类查询品牌
     * */
    @Override
    public List<Brand> queryBrandsByCid(Long cid) {
        List<Long> bids = brandMapper.queryBidsByCid(cid);
        if (CollectionUtils.isEmpty(bids)){
            return null;
        }
        List<Brand> brands=brandMapper.selectByIdList(bids);
        if (CollectionUtils.isEmpty(brands)){
            return null;
        }
        return brands;
    }

    /**
     * 根据品牌id查询品牌
     * */
    @Override
    public Brand queryBrandById(Long id) {
        return this.brandMapper.selectByPrimaryKey(id);
    }
}
