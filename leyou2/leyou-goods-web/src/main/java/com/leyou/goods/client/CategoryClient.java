package com.leyou.goods.client;

import com.leyou.item.api.CategorysApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("leyou-item-service")
public interface CategoryClient extends CategorysApi {

}
