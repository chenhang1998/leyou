package com.leyou.search.service;

import com.leyou.item.pojo.Spu;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;

import java.io.IOException;

public interface SearchService {

    /**
     * 构建Goods
     * */
    Goods buildGoods(Spu spu) throws IOException;

    SearchResult search(SearchRequest request);

    /**
     * 增添，修改消息队列的监听
     * */
    void save(Long spuId) throws IOException;

    /**
     * 删除消息队列的监听
     * */
    void delete(Long spuId) throws IOException;
}
