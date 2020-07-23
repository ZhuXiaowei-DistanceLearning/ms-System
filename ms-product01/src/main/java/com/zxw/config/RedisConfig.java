package com.zxw.config;

import com.zxw.constant.RedisKeyPrefix;
import com.zxw.mapper.ProductMapper;
import com.zxw.pojo.ProductPo;
import com.zxw.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.PostConstruct;

/**
 * @author zxw
 * @date 2019/9/8 13:19
 */
@Configuration
public class RedisConfig {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductMapper productMapper;

    @PostConstruct
    public void initAllGoods() {
        redisTemplate.delete(redisTemplate.keys("*"));
        ProductPo byId = productMapper.findById(1L);
        ProductPo byId1 = productMapper.findById(2L);
        redisTemplate.opsForValue().set(RedisKeyPrefix.SECKILL_GOODS + byId.getId(), JsonUtils.serialize(byId));
        redisTemplate.opsForValue().set(RedisKeyPrefix.SECKILL_GOODS + byId1.getId(), JsonUtils.serialize(byId1));
        redisTemplate.opsForValue().set(RedisKeyPrefix.SECKILL_INVENTORY + byId.getId(), String.valueOf(byId.getStock()));
        redisTemplate.opsForValue().set(RedisKeyPrefix.SECKILL_INVENTORY + byId1.getId(), String.valueOf(byId1.getStock()));
    }
}
