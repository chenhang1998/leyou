package com.leyou.goods.client;

import com.leyou.item.api.BrandsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("leyou-item-service")
public interface BrandClient extends BrandsApi {

}
