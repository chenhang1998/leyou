package com.leyou.item.service;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;

import java.util.List;

public interface BrandService {

    /**
     * 根据查询条件分页并排序查询品牌信息
     * */
    PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc);
    
    /**
     *新增品牌
     * brand
     * cids
     */
    void savebrand(Brand brand, List<Long> cids);

    /**
     * 根据商品分类查询品牌
     * */
    List<Brand> queryBrandsByCid(Long cid);

    Brand queryBrandById(Long id);
}
