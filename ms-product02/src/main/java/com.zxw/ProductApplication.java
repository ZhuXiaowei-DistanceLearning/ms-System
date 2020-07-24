package com.zxw;

import com.zxw.loadbalancer.IRule;
import com.zxw.loadbalancer.RoundRobinRule;
import com.zxw.lock.DistributedLocker;
import com.zxw.lock.RedissonDistributedLocker;
import com.zxw.utils.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

/**
 * @author zxw
 * @date 2020/7/22 14:09
 */
@SpringCloudApplication
@EnableDiscoveryClient
public class ProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class,args);
    }

    @Bean
    public IRule roundRobin(){
        return new RoundRobinRule();
    }

    @Bean
    public DistributedLocker distributedLocker(){
        return new RedissonDistributedLocker();
    }

    @Bean
    public IdWorker idWorker(){return new IdWorker();}

}
