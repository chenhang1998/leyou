package com.leyou.goods.service;

import java.util.Map;

public interface GoodsService {

    /**
     *根据cid查询商品详情页的数据
     * */
    Map<String,Object> loadData(Long cid);
}
