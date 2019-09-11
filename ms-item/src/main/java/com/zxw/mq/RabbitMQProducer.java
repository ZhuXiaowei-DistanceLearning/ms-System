package com.zxw.mq;

import com.rabbitmq.client.ConfirmCallback;
import com.zxw.pojo.MessageVo;
import com.zxw.utils.JsonUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author zxw
 * @date 2019/9/11 19:31
 */
@Component
public class RabbitMQProducer implements ConfirmCallback {
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void send(MessageVo messageVo) {
        String s = JsonUtils.serialize(messageVo);
        amqpTemplate.convertAndSend("purchase_queue", s);
        boolean sendAcked = false;

    }

    @Override
    public void handle(long l, boolean b) throws IOException {
        System.out.println("----------");
        System.out.println("消息发送失败，重新发送");
        System.out.println(l);
        System.out.println(b);
        System.out.println("----------");
    }
}
