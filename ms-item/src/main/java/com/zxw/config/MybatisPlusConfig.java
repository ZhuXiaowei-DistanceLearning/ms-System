package com.zxw.config;

import com.baomidou.mybatisplus.extension.plugins.PerformanceInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author zxw
 * @date 2019/9/4 15:27
 */
@EnableTransactionManagement
@Configuration
public class MybatisPlusConfig {
    /**
     * SQL执行效率插件
     */
    @Bean
//    @Profile({"dev","test"})// 设置 dev test 环境开启
    public PerformanceInterceptor performanceInterceptor() {
        return new PerformanceInterceptor();
    }
}
