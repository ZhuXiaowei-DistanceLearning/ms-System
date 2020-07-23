package com.zxw.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author zxw
 * @date 2020/7/22 13:43
 */
@Component
public class GlobalFilters implements GlobalFilter {
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 用户访问到一定次数后进行拦截
        // 如果商品已经
        return chain.filter(exchange);
    }
}
