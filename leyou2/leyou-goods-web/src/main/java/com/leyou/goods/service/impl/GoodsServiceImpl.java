package com.leyou.goods.service.impl;

import com.leyou.goods.client.BrandClient;
import com.leyou.goods.client.CategoryClient;
import com.leyou.goods.client.GoodsClient;
import com.leyou.goods.client.SpecificationClient;
import com.leyou.goods.service.GoodsService;
import com.leyou.item.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;

    /**
     *根据cid查询商品详情页的数据
     * */
    public Map<String,Object> loadData(Long id){
        Map<String,Object> model=new HashMap<>();

        //查询spu
        Spu spu = this.goodsClient.querySpuBySpuId(id);

        //查询spuDetail
        SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(id);

        //查询分类
        List<Long> list = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
        List<String> names = this.categoryClient.queryNameByIds(list);
        List<Map<String,Object>> categories=new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String,Object> map=new HashMap<>();
            map.put("id",list.get(i));
            map.put("name",names.get(i));
            categories.add(map);
        }

        //查询brand
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());

        //查询skus
        List<Sku> skus = this.goodsClient.querySkusBySpuId(id);

        //查询groups
        List<SpecGroup> groups = this.specificationClient.queryGroupsWithParam(spu.getCid3());

        //查询paramMap
        List<SpecParam> params = this.specificationClient.queryParams(null,spu.getCid3(),false,null);
        Map<Long,String> paramMap =new HashMap<>();
        params.forEach(param ->{
            paramMap.put(param.getId(),param.getName());
        });

        //查询结果放入model并返回
        model.put("spu",spu);
        model.put("spuDetail",spuDetail);
        model.put("categories",categories);
        model.put("brand",brand);
        model.put("skus",skus);
        model.put("groups",groups);
        model.put("paramMap",paramMap);
        return model;
    }
}
