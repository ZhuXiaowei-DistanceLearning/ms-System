package com.zxw;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.zxw.utils.IdWorker;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@MapperScan("com.zxw.mapper")
@EnableDiscoveryClient
/**
 * @author zxw
 * @date 2019/9/6 21:05
 */
public class ItemApplication {
    public static void main(String[] args) {
//        initFlowRules();
        SpringApplication.run(ItemApplication.class,args);
    }

    private static void initFlowRules(){
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setResource("purchase");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(200);
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }

    @Bean
    public IdWorker idWorker(){
        return new IdWorker();
    }
}
