package com.zxw.service;

import com.zxw.constant.RedisKey;
import com.zxw.constant.RedisKeyPrefix;
import com.zxw.loadbalancer.IRule;
import com.zxw.lock.DistributedLocker;
import com.zxw.mq.RabbitMQProducer;
import com.zxw.pojo.MessageVo;
import com.zxw.pojo.ProductPo;
import com.zxw.utils.IdWorker;
import com.zxw.utils.JsonUtils;
import com.zxw.utils.MD5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author zxw
 * @date 2019/9/3 15:24
 */
@Service
public class PurchaseService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RabbitMQProducer mqProducer;

    @Autowired
    private IRule roundRobin;

    @Autowired
    private DistributedLocker distributedLocker;


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


    /**
     * 添加redis
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean purchase(Long userId, Long productId, int quantity) {
        Object[] keys = redisTemplate.keys(RedisKeyPrefix.MS_REDIS_PREFIX + productId + "_" + "?").toArray();
        // 获取分布式锁
        int keyCount = keys.length;
        if (keyCount == 0) {
            return false;
        }
        int index = roundRobin.choose(keyCount);
        distributedLocker.lock(keys[index] + "_" + index, TimeUnit.SECONDS, 5);
//        String s = redisTemplate.opsForValue().get(RedisKeyPrefix.BOUGHT_USERS + ai);
        Integer total = Integer.valueOf(redisTemplate.opsForValue().get(RedisKeyPrefix.SECKILL_INVENTORY + productId));
        Boolean s1 = redisTemplate.opsForSet().isMember(RedisKeyPrefix.BOUGHT_USERS + productId, String.valueOf(userId));
        // 购买用户
        if (s1) {
            System.out.println("已经购买过，请勿重复购买");
            return false;
        }
        // 库存总量
        if (total <= 0) {
            System.out.println("库存不足");
            // 秒杀结束
            return false;
        }
        // 预减库存
        redisTemplate.opsForValue().increment(RedisKeyPrefix.SECKILL_INVENTORY + productId, -1);
        // 异步下单
        MessageVo messageVo = new MessageVo();
        messageVo.setUserId(userId);
        messageVo.setGoodsId(productId);
        messageVo.setQuantity(1);
        mqProducer.send(messageVo);
        return true;
    }

    public String expore(long goodsId) throws ParseException {
        String s = redisTemplate.opsForValue().get(RedisKeyPrefix.SECKILL_GOODS + goodsId);
        if (s == null) {
            return "";
        }
        ProductPo po = JsonUtils.parse(s, ProductPo.class);
        Date date = po.getBeginTime();
        Date d = new Date();
        System.out.println(d.getTime());
        if (date.getTime() < date.getTime()) {
            return "false";
        }
        String md5 = redisTemplate.opsForValue().get(RedisKey.MD5);
        if (md5 == null) {
            md5 = MD5.MD5Encode(String.valueOf(idWorker.nextId()));
            redisTemplate.opsForValue().set(RedisKey.MD5, md5, 60, TimeUnit.SECONDS);
        }
        return md5;
    }

    public String isGrab(long goodsId, long userId) {
        String result = "0";
        result = redisTemplate.opsForSet().isMember(RedisKeyPrefix.BOUGHT_USERS + goodsId, String.valueOf(userId)) ? "1" : "0";
        return result;
    }
}
