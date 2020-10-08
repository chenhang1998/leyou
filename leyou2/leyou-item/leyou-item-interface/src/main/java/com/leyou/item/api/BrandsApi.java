package com.leyou.item.api;

import com.leyou.item.pojo.Brand;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/brand")
public interface BrandsApi {

    /**
     * 根据品牌id查询品牌
     * */
    @GetMapping("/{id}")
    public Brand queryBrandById(@PathVariable("id")Long id);
}
