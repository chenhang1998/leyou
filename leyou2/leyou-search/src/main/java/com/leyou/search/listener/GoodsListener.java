package com.leyou.search.listener;

import com.leyou.search.service.SearchService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GoodsListener {
    @Autowired
    private SearchService searchService;

    /**
     * 增添，修改消息队列的监听
     * */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "leyou.search.save.queue",durable = "true"),
            exchange = @Exchange(value = "leyou.item.exchange",type = ExchangeTypes.TOPIC,ignoreDeclarationExceptions = "true"),
            key = {"item.insert","item.update"}
    ))
    public void save(Long spuId) throws IOException {
        if (spuId==null){
            return;
        }
        this.searchService.save(spuId);
    }

    /**
     * 删除消息队列的监听
     * */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "leyou.search.delete.queue",durable = "true"),
            exchange = @Exchange(value = "leyou.item.exchange",type = ExchangeTypes.TOPIC,ignoreDeclarationExceptions = "true"),
            key = {"item.delete"}
    ))
    public void delete(Long spuId) throws IOException {
        if (spuId==null){
            return;
        }
        this.searchService.delete(spuId);
    }
}
