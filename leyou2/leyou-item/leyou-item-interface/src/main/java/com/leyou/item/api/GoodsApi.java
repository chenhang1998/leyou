package com.leyou.item.api;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

public interface GoodsApi {
    /**
     * 根据spuId查询spuDetail
     * */
    @GetMapping("/spu/detail/{spuId}")
    @ResponseBody
    public SpuDetail querySpuDetailBySpuId(@PathVariable("spuId")Long spuId);

    /**
     * 查询商品列表
     * */
    @GetMapping("/spu/page")
    @ResponseBody
    public PageResult<SpuBo> querySpuByPage(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
            @RequestParam(value = "key",required = false)String key,
            @RequestParam(value = "saleable",required = false)Boolean saleable
    );

    /**
     * 根据spuId查询Skus
     * */
    @GetMapping("/sku/list")
    @ResponseBody
    public List<Sku> querySkusBySpuId(@RequestParam("id")Long spuId);

    /**
     * 根据spuId查询Spu
     * */
    @GetMapping("/{id}")
    @ResponseBody
    public Spu querySpuBySpuId(@PathVariable("id")Long id);
}
