package com.zxw.service;

import com.zxw.mapper.ProductMapper;
import com.zxw.mapper.PurchaseMapper;
import com.zxw.pojo.ProductPo;
import com.zxw.pojo.PurchaseRecoredPo;
import com.zxw.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zxw
 * @date 2019/9/3 15:24
 */
@Service
public class OrderService {
    static AtomicInteger ai = new AtomicInteger(0);
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private PurchaseMapper purchaseMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;

    private static ConcurrentHashMap map = new ConcurrentHashMap();

    public boolean insertDate(long goodsId, long userId, long quantity) {
        // 订单服务
        // 库存服务
        // 积分服务
        // 仓储服务
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
