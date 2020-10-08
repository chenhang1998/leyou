package com.leyou.item.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping(path = "/category")
public interface CategorysApi {

    /**
     * 根据ids查询多个分类名称
     * */
    @GetMapping
    public List<String> queryNameByIds(@RequestParam("ids")List<Long> ids);
}
