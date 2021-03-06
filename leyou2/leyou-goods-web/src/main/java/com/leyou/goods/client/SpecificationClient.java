package com.leyou.goods.client;

import com.leyou.item.api.SpecificationsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("leyou-item-service")
public interface SpecificationClient extends SpecificationsApi {

}
