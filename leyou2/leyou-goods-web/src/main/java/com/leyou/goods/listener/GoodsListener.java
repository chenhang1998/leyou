package com.leyou.goods.listener;

import com.leyou.goods.service.GoodsHtmlService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoodsListener {
    @Autowired
    private GoodsHtmlService goodsHtmlService;

    /**
     * 增添，修改消息队列的监听
     * */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "leyou.item.save.queue",durable = "true"),           //声明消息队列，并且持久化
            exchange = @Exchange(value = "leyou.item.exchange",type = ExchangeTypes.TOPIC,ignoreDeclarationExceptions = "true"),     //声明交换机，默认持久化，类型为topic，忽略异常
            key = {"item.insert","item.update"}
    ))
    public void save(Long spuId){
        if (spuId==null){
            return;
        }
        goodsHtmlService.createHtml(spuId);
    }

    /**
     * 删除消息队列的监听
     * */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "leyou.item.delete.queue",durable = "true"),           //声明消息队列，并且持久化
            exchange = @Exchange(value = "leyou.item.exchange",type = ExchangeTypes.TOPIC,ignoreDeclarationExceptions = "true"),     //声明交换机，默认持久化，类型为topic，忽略异常
            key = {"item.delete"}
    ))
    public void delete(Long spuId){
        if (spuId==null){
            return;
        }
        goodsHtmlService.deleteHtml(spuId);
    }
}
