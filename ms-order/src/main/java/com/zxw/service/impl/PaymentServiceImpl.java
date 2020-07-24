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

import com.zxw.client.AccountClient;
import com.zxw.client.InventoryClient;
import com.zxw.dto.AccountDTO;
import com.zxw.dto.InventoryDTO;
import com.zxw.entity.Order;
import com.zxw.enums.OrderStatusEnum;
import com.zxw.mapper.OrderMapper;
import com.zxw.service.PaymentService;
import org.dromara.hmily.annotation.Hmily;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * PaymentServiceImpl.
 *
 * @author xiaoyu
 */
@Service
@SuppressWarnings("all")
public class PaymentServiceImpl implements PaymentService {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final OrderMapper orderMapper;

    private final AccountClient accountClient;

    private final InventoryClient inventoryClient;

    @Autowired(required = false)
    public PaymentServiceImpl(OrderMapper orderMapper,
                              AccountClient accountClient,
                              InventoryClient inventoryClient) {
        this.orderMapper = orderMapper;
        this.accountClient = accountClient;
        this.inventoryClient = inventoryClient;
    }

    @Override
    @Hmily(confirmMethod = "confirmOrderStatus", cancelMethod = "cancelOrderStatus")
    public void makePayment(Order order) {
        // 对本地订单数据库修改服务状态为UPDATING
        // --- Try阶段 ---:不直接完成业务操作，完成一个Try操作，锁定某个资源或者预备类状态
        // --- Confirm阶段 ---
        //
        // 调用库存服务
        // 调用积分服务
        // 调用仓储服务
        order.setStatus(OrderStatusEnum.PAYING.getCode());
        orderMapper.update(order);
        //检查数据
        final BigDecimal accountInfo = accountClient.findByUserId(order.getUserId());

        final Integer inventoryInfo = inventoryClient.findByProductId(order.getProductId());

        if (accountInfo.compareTo(order.getTotalAmount()) < 0) {
            throw new HmilyRuntimeException("余额不足！");
        }

        if (inventoryInfo < order.getCount()) {
            throw new HmilyRuntimeException("库存不足！");
        }

        //扣除用户余额

        //进入扣减库存操作
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setCount(order.getCount());
        inventoryDTO.setProductId(order.getProductId());
        inventoryClient.decrease(inventoryDTO);

        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAmount(order.getTotalAmount());
        accountDTO.setUserId(order.getUserId());
        LOGGER.debug("===========执行springcloud扣减资金接口==========");
        accountClient.payment(accountDTO);
    }

    @Override
    @Hmily(confirmMethod = "confirmOrderStatus", cancelMethod = "cancelOrderStatus")
    public String mockPaymentInventoryWithTryException(Order order) {
        LOGGER.debug("===========执行springcloud  mockPaymentInventoryWithTryException 扣减资金接口==========");
        order.setStatus(OrderStatusEnum.PAYING.getCode());
        orderMapper.update(order);
        //扣除用户余额
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAmount(order.getTotalAmount());
        accountDTO.setUserId(order.getUserId());
        accountClient.payment(accountDTO);
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setCount(order.getCount());
        inventoryDTO.setProductId(order.getProductId());
        inventoryClient.mockWithTryException(inventoryDTO);
        return "success";
    }

    @Override
    @Hmily(confirmMethod = "confirmOrderStatus", cancelMethod = "cancelOrderStatus")
    public String mockPaymentInventoryWithTryTimeout(Order order) {
        LOGGER.debug("===========执行springcloud  mockPaymentInventoryWithTryTimeout 扣减资金接口==========");
        order.setStatus(OrderStatusEnum.PAYING.getCode());
        orderMapper.update(order);
        //扣除用户余额
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAmount(order.getTotalAmount());
        accountDTO.setUserId(order.getUserId());
        accountClient.payment(accountDTO);
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setCount(order.getCount());
        inventoryDTO.setProductId(order.getProductId());
        inventoryClient.mockWithTryTimeout(inventoryDTO);
        return "success";
    }

    public void confirmOrderStatus(Order order) {
        order.setStatus(OrderStatusEnum.PAY_SUCCESS.getCode());
        orderMapper.update(order);
        LOGGER.info("=========进行订单confirm操作完成================");
    }

    public void cancelOrderStatus(Order order) {
        order.setStatus(OrderStatusEnum.PAY_FAIL.getCode());
        orderMapper.update(order);
        LOGGER.info("=========进行订单cancel操作完成================");
    }

}
