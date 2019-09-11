package com.zxw.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.zxw.constant.RedisKey;
import com.zxw.constant.RedisKeyPrefix;
import com.zxw.exception.ExceptionUtil;
import com.zxw.mapper.ProductMapper;
import com.zxw.mapper.PurchaseMapper;
import com.zxw.mq.RabbitMQProducer;
import com.zxw.pojo.MessageVo;
import com.zxw.pojo.ProductPo;
import com.zxw.pojo.PurchaseRecoredPo;
import com.zxw.utils.IdWorker;
import com.zxw.utils.JsonUtils;
import com.zxw.utils.MD5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
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
    private StringRedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RabbitMQProducer mqProducer;

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
    @SentinelResource(value = "purchase", blockHandler = "handleException", blockHandlerClass = {ExceptionUtil.class})
    public synchronized boolean purchase(Long userId, Long productId, int quantity) {
        int i = ai.incrementAndGet();
//        String s = redisTemplate.opsForValue().get(RedisKeyPrefix.BOUGHT_USERS + ai);
        String s = redisTemplate.opsForValue().get(RedisKeyPrefix.SECKILL_INVENTORY + productId);
        Integer total = Integer.valueOf(s);
        if (total < 1) {
            System.out.println("库存不足");
            return false;
        }
        Boolean s1 = redisTemplate.opsForSet().isMember(RedisKeyPrefix.BOUGHT_USERS + productId, String.valueOf(i));
        if (s1) {
            System.out.println("已经购买过，请勿重复购买");
            return false;
        }
        long start = System.currentTimeMillis();
        // 预减库存
        redisTemplate.opsForValue().increment(RedisKeyPrefix.SECKILL_INVENTORY + productId, -1);
        MessageVo messageVo = new MessageVo();
        messageVo.setUserId(userId);
        messageVo.setGoodsId(productId);
        messageVo.setQuantity(1);
        mqProducer.send(messageVo);
        System.out.println("购买成功人数:" + ai.get());
        return true;
    }

    public String expore(long goodsId) throws ParseException {
        String s = redisTemplate.opsForValue().get(RedisKeyPrefix.SECKILL_GOODS + goodsId);
        if (s == null) {
            ProductPo id = productMapper.findById(goodsId);
            if (id == null) {
                return "false";
            } else {
                redisTemplate.opsForValue().set(RedisKeyPrefix.SECKILL_GOODS + goodsId, JsonUtils.serialize(id));
            }
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

    public boolean insertDate(long goodsId, long userId, long quantity) {
        try {
            ProductPo byId = productMapper.findById(goodsId);
            productMapper.decr((int) quantity, byId.getVersion(), goodsId);
            PurchaseRecoredPo pr = new PurchaseRecoredPo();
            pr.setId(idWorker.nextId());
            pr.setNote("购买日志，时间：" + System.currentTimeMillis());
            pr.setProduct_id(goodsId);
            pr.setQuantity((int) quantity);
            pr.setUser_id(userId);
            pr.setPrice(5.0);
            pr.setSum(15.0);
            purchaseMapper.insert(pr);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
