package com.leyou.goods.service;

public interface GoodsHtmlService {

    /**
     * 静态html保存本地服务器
     * */
    void createHtml(Long spuId);

    /**
     * 删除本地服务器保存的html静态页面
     * */
    void deleteHtml(Long spuId);
}
