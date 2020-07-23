package com.zxw.mq;

import com.zxw.pojo.MessageVo;
import com.zxw.utils.JsonUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author zxw
 * @date 2019/9/11 19:31
 */
@Component
public class RabbitMQProducer {
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void send(MessageVo messageVo) {
        String s = JsonUtils.serialize(messageVo);
        amqpTemplate.convertAndSend("purchase_queue", s);
        boolean sendAcked = false;

    }
}
