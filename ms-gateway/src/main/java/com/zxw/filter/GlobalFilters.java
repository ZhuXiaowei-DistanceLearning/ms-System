package com.zxw.filter;

import com.zxw.consts.RedisKeyPrefix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author zxw
 * @date 2020/7/22 13:43
 */
@Component
public class GlobalFilters implements GlobalFilter {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 用户访问到一定次数后进行拦截
//        RedissonUtils.IpLimit(RedisKey.IPLIMT, , );
        // 如果商品已经

        String s = redisTemplate.opsForValue().get(RedisKeyPrefix.SECKILL_GOODS + "goodsId");
        if (s == null) {
            return chain.filter(exchange);
        }
        return chain.filter(exchange);
    }
}
