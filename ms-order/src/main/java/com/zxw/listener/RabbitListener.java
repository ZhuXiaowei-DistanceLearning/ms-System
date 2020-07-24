package com.zxw.listener;

import com.zxw.constant.RedisKeyPrefix;
import com.zxw.pojo.MessageVo;
import com.zxw.service.OrderService;
import com.zxw.utils.JsonUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author zxw
 * @date 2019/9/8 15:41
 */
@Component
public class RabbitListener {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private OrderService orderService;


    @org.springframework.amqp.rabbit.annotation.RabbitListener(bindings = @QueueBinding(value = @Queue(value = "purchase_queue", durable = "true"), exchange = @Exchange(value = "purchase_exchange", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC), key = {"#.#"}))
    public void listen(String msg) {
        MessageVo vo = JsonUtils.parse(msg, MessageVo.class);
        System.out.println("Receiver:[" + msg + "]");
        redisTemplate.opsForSet().add(RedisKeyPrefix.BOUGHT_USERS + vo.getGoodsId(), String.valueOf(vo.getUserId()));
        orderService.insertDate(vo.getGoodsId(), vo.getUserId(), vo.getQuantity());
    }
}
