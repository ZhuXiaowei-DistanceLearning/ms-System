/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zxw.service.impl;

import com.zxw.entity.Order;
import com.zxw.enums.OrderStatusEnum;
import com.zxw.mapper.OrderMapper;
import com.zxw.mapper.PurchaseMapper;
import com.zxw.service.OrderService;
import com.zxw.service.PaymentService;
import com.zxw.utils.IdWorker;
import org.dromara.hmily.annotation.Hmily;
import org.dromara.hmily.common.utils.IdWorkerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;


/**
 * @author xiaoyu
 */
@Service("orderService")
@SuppressWarnings("all")
public class OrderServiceImpl implements OrderService {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PurchaseMapper purchaseMapper;

    @Autowired
    IdWorker idWorker;

    @Override
    @Hmily(confirmMethod = "confirmOrderStatus", cancelMethod = "cancelOrderStatus")
    public String orderPay(Integer count, BigDecimal amount) {
        final Order order = buildOrder(count, amount);
        final int rows = orderMapper.save(order);

        if (rows > 0) {
            paymentService.makePayment(order);
        }
        return "success";
    }

    /**
     * 模拟在订单支付操作中，库存在try阶段中的库存异常
     *
     * @param count  购买数量
     * @param amount 支付金额
     * @return string
     */
    @Override
    public String mockInventoryWithTryException(Integer count, BigDecimal amount) {
        final Order order = buildOrder(count, amount);
        final int rows = orderMapper.save(order);

        if (rows > 0) {
            paymentService.mockPaymentInventoryWithTryException(order);
        }


        return "success";
    }

    /**
     * 模拟在订单支付操作中，库存在try阶段中的timeout
     *
     * @param count  购买数量
     * @param amount 支付金额
     * @return string
     */
    @Override
    public String mockInventoryWithTryTimeout(Integer count, BigDecimal amount) {
        final Order order = buildOrder(count, amount);
        final int rows = orderMapper.save(order);

        if (rows > 0) {
            paymentService.mockPaymentInventoryWithTryTimeout(order);
        }


        return "success";
    }


    @Override
    public void updateOrderStatus(Order order) {
        orderMapper.update(order);
    }

    @Override
    public void insertOrder(int productId, int userId, int quantity) {
        try {
//            ProductPo byId = productMapper.findById(goodsId);
//            productMapper.decr((int) quantity, byId.getVersion(), goodsId);
//            PurchaseRecoredPo pr = new PurchaseRecoredPo();
//            pr.setId(idWorker.nextId());
//            pr.setNote("购买日志，时间：" + System.currentTimeMillis());
//            pr.setProduct_id(goodsId);
//            pr.setQuantity((int) quantity);
//            pr.setUser_id(userId);
//            pr.setPrice(5.0);
//            pr.setSum(15.0);
//            purchaseMapper.insert(pr);
            Order order = new Order();
            order.setCreateTime(new Date());
            order.setNumber(IdWorkerUtils.getInstance().buildPartNumber());
            order.setProductId(productId);
            order.setStatus(OrderStatusEnum.NOT_PAY.getCode());
            order.setTotalAmount(new BigDecimal(5));
            order.setCount(quantity);
            order.setNote("购买日志，时间：" + System.currentTimeMillis());
            //demo中 表里面存的用户id为10000
            order.setUserId("10000");
            int save = orderMapper.save(order);
            if (save > 0) {
                paymentService.makePayment(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Order buildOrder(Integer count, BigDecimal amount) {
        LOGGER.debug("构建订单对象");
        Order order = new Order();
        order.setCreateTime(new Date());
        order.setNumber(IdWorkerUtils.getInstance().buildPartNumber());
        //demo中的表里只有商品id为 1的数据
        order.setProductId(1);
        order.setStatus(OrderStatusEnum.NOT_PAY.getCode());
        order.setTotalAmount(amount);
        order.setCount(count);
        //demo中 表里面存的用户id为10000
        order.setUserId("10000");
        return order;
    }

    public void confirmOrderStatus(Order order) {
        order.setStatus(OrderStatusEnum.PAYING.getCode());
        orderMapper.update(order);
        LOGGER.info("=========try================");
    }

    public void cancelOrderStatus(Order order) {
        order.setStatus(OrderStatusEnum.PAYING.getCode());
        orderMapper.update(order);
        LOGGER.info("=========进行订单cancel操作完成================");
    }
}
