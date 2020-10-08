package com.leyou.item.service;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;

import java.util.List;

public interface GoodsService {
    PageResult<SpuBo> querySpuByPage(Integer page, Integer rows, String key, Boolean saleable);

    /**
     * 添加商品
     * */
    void saveGoods(SpuBo spuBo);

    /**
     * 根据spuId查询spuDetail
     * */
    SpuDetail querySpuDetailBySpuId(Long spuId);

    /**
     * 根据spuId查询Skus
     * */
    List<Sku> querySkusBySpuId(Long spuId);

    /**
     * 编辑商品
     * */
    void updateGoods(SpuBo spuBo);

    /**
     * 根据spuId查询Spu
     * */
    Spu querySpuBySpuId(Long spuId);
}
