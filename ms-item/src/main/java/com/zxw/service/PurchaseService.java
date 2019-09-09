package com.zxw.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.zxw.constant.RedisKeyPrefix;
import com.zxw.exception.ExceptionUtil;
import com.zxw.mapper.ProductMapper;
import com.zxw.mapper.PurchaseMapper;
import com.zxw.pojo.PurchaseRecoredPo;
import com.zxw.utils.IdWorker;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zxw
 * @date 2019/9/3 15:24
 */
@Service
public class PurchaseService {
    static AtomicInteger ai = new AtomicInteger(0);
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private PurchaseMapper purchaseMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;

    private static ConcurrentHashMap map = new ConcurrentHashMap();

   /* @Transactional(rollbackFor = Exception.class)
    public synchronized boolean purchase(Long userId, Long productId, int quantity) {
        // 获取产品
        ProductPo po = null;
        po = productMapper.findById(productId);
        // 比较库存和购买数量
        if (po.getStock() < quantity) {
            // 库存不足
            System.out.println("库存不足");
            return false;
        }
        int version = po.getVersion();
        // 扣减库存
        UpdateWrapper<ProductPo> wrapper = new UpdateWrapper<>();
        wrapper.set("stock", po.getStock() - quantity);
        productMapper.update(po, wrapper);
//        int decr = productMapper.decr(quantity, version, userId);
        ai.incrementAndGet();
        // 初始化购买记录
        PurchaseRecoredPo pr = new PurchaseRecoredPo();
        pr.setNote("购买日志，时间：" + System.currentTimeMillis());
        pr.setPrice(po.getPrice());
        pr.setProduct_id(po.getId());
        pr.setQuantity(quantity);
        double sum = po.getPrice() * quantity;
        pr.setSum(sum);
        pr.setUser_id(1L);
        // 插入购买记录
        purchaseMapper.insert(pr);
        System.out.println("购买成功人数:" + ai.get());
        return true;
    }*/

//    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
//    public boolean purchase(Long userId, Long productId, int quantity) {
//        long start = System.currentTimeMillis();
//        for (int i = 0; i < 3; i++) {
//            long end = System.currentTimeMillis();
//            if (end - start > 100) {
//                return false;
//            }
//            // 获取产品
//            ProductPo po = null;
//            po = productMapper.findById(productId);
//            // 比较库存和购买数量
//            if (po.getStock() < quantity) {
//                // 库存不足
//                System.out.println("库存不足");
//                return false;
//            }
//            int version = po.getVersion();
//            // 扣减库存
////        UpdateWrapper<ProductPo> wrapper = new UpdateWrapper<>();
////        wrapper.set("stock", po.getStock() - quantity);
////        productMapper.update(po, wrapper);
//            int decr = productMapper.decr(quantity, version, userId);
//            if (decr == 0) {
//                continue;
//            }
//            ai.incrementAndGet();
//            // 初始化购买记录
//            PurchaseRecoredPo pr = new PurchaseRecoredPo();
//            pr.setNote("购买日志，时间：" + System.currentTimeMillis());
//            pr.setPrice(po.getPrice());
//            pr.setProduct_id(po.getId());
//            pr.setQuantity(quantity);
//            double sum = po.getPrice() * quantity;
//            pr.setSum(sum);
//            pr.setUser_id(1L);
//            // 插入购买记录
//            purchaseMapper.insert(pr);
//            System.out.println("购买成功人数:" + ai.get());
//            return true;
//        }
//        return false;
//    }


    @Transactional(rollbackFor = Exception.class)
    /**
     * 添加redis
     */
    @SentinelResource(value = "purchase", blockHandler = "handleException",blockHandlerClass = {ExceptionUtil.class})
    public synchronized boolean purchase(Long userId, Long productId, int quantity) {
        int i = ai.incrementAndGet();
//        String s = redisTemplate.opsForValue().get(RedisKeyPrefix.BOUGHT_USERS + ai);
        String s = redisTemplate.opsForValue().get(RedisKeyPrefix.SECKILL_INVENTORY + productId);
        Integer total = Integer.valueOf(s);
        if (total < 3) {
            System.out.println("库存不足");
            return false;
        }
        Boolean s1 = redisTemplate.opsForSet().isMember(RedisKeyPrefix.BOUGHT_USERS + productId, String.valueOf(i));
        if (s1) {
            System.out.println("已经购买过，请勿重复购买");
            return false;
        }
        long start = System.currentTimeMillis();
        // 获取产品
        redisTemplate.opsForValue().increment(RedisKeyPrefix.SECKILL_INVENTORY + productId, -3);
        // 初始化购买记录
        PurchaseRecoredPo pr = new PurchaseRecoredPo();
        pr.setId(idWorker.nextId());
        pr.setNote("购买日志，时间：" + System.currentTimeMillis());
        pr.setProduct_id(productId);

        pr.setQuantity(quantity);
        pr.setUser_id(ai.longValue());
        pr.setPrice(5.0);
        pr.setSum(15.0);
        // 插入购买记录
//        redisTemplate.opsForValue().set(RedisKeyPrefix.BOUGHT_USERS + userId, JsonUtils.serialize(pr));
        redisTemplate.opsForSet().add(RedisKeyPrefix.BOUGHT_USERS + productId, ai.toString());
        System.out.println("购买成功人数:" + ai.get());
        return true;
    }
}
