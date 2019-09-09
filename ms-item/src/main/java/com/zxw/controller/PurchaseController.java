package com.zxw.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.zxw.constant.RedisKeyPrefix;
import com.zxw.mapper.ProductMapper;
import com.zxw.service.PurchaseService;
import com.zxw.utils.IdWorker;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Set;

/**
 * @author zxw
 * @date 2019/9/3 15:43
 */
@Controller
public class PurchaseController {
    @Autowired
    PurchaseService purchaseService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductMapper productMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private static RateLimiter rl = RateLimiter.create(2);

    @GetMapping("/test")
    public String testPage(Model model) {
        String substring = RedisKeyPrefix.BOUGHT_USERS.substring(0, RedisKeyPrefix.BOUGHT_USERS.length() - 1);
        Set<String> keys = redisTemplate.keys(substring+":/**");
        model.addAttribute("msg", "Hello, Thymeleaf!");
        return "index";
    }

    @PostMapping("/purchase")
    @ResponseBody
    public ResponseEntity<String> purchase(Long userId, Long productId, Integer quantity) {
        boolean success = purchaseService.purchase(userId, productId, quantity);
        String message = success ? "抢购成功" : "抢购失败";
        return ResponseEntity.ok(message);
    }


}
