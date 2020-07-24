package com.zxw.filter;

import com.zxw.consts.RedisKey;
import com.zxw.consts.RedisKeyPrefix;
import com.zxw.utils.RedissonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * @author zxw
 * @date 2020/7/22 13:43
 */
@Component
public class GlobalFilters implements GlobalFilter {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String productId = exchange.getRequest().getQueryParams().getFirst("productId");
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        // 访问限流
        Long limit = RedissonUtils.IpLimit(RedisKey.IPLIMT, remoteAddress.getHostName(), 3);
        if(limit == 0){
            System.out.println("达到限制访问次数，已限制");
            return chain.filter(exchange);
        }
        String s = redisTemplate.opsForValue().get(RedisKeyPrefix.SECKILL_GOODS + productId);
        // 商品秒杀已结束
        if (s == null) {
            return chain.filter(exchange);
        }
        // 缓存穿透
        return chain.filter(exchange);
    }
}
