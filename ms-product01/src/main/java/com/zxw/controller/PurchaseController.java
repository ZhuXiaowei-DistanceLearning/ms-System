package com.zxw.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.util.concurrent.RateLimiter;
import com.zxw.constant.RedisKeyPrefix;
import com.zxw.mapper.ProductMapper;
import com.zxw.pojo.ProductPo;
import com.zxw.service.PurchaseService;
import com.zxw.utils.IdWorker;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.util.List;

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
        QueryWrapper<ProductPo> wrapper = new QueryWrapper<ProductPo>();
        List<ProductPo> po = productMapper.selectList(wrapper);
        model.addAttribute("msg", po);
        return "index";
    }

    @GetMapping("/exposer/{goodsId}")
    @ResponseBody
    public String expore(@PathVariable("goodsId") long goodsId) throws ParseException {
        String md5 = purchaseService.expore(goodsId);
        return md5;
    }

    @PostMapping("/purchase")
    @ResponseBody
    public ResponseEntity<String> purchase(Long userId, Long productId, Integer quantity, String md5) {
        Object[] keys = redisTemplate.keys(RedisKeyPrefix.MS_REDIS_PREFIX + "?").toArray();
        // 如果列表数量为0，则返回空
        int keyCount = keys.length;
        if (keyCount == 0) {
            return ResponseEntity.ok(null);
        }
//        int index = this.incrementAndGetModulo(keyCount);
        boolean success = purchaseService.purchase(userId, productId, quantity);
        String message = success ? String.valueOf(productId) : "fail";
        return ResponseEntity.ok(message);
    }

    @GetMapping("/isGrab/{goodsId}/{userId}")
    @ResponseBody
    public ResponseEntity<String> isGrab(@PathVariable("goodsId") long goodsId, @PathVariable("userId") long userId) {
        String result = purchaseService.isGrab(goodsId,userId);
        return ResponseEntity.ok(result);
    }

}
