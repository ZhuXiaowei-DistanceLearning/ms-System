package com.zxw;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author zxw
 * @date 2020/7/22 14:09
 */
@SpringCloudApplication
@EnableDiscoveryClient
public class WarehouseApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseApplication.class, args);
    }
}
