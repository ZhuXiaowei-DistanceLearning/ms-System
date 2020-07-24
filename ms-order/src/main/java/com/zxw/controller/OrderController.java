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

package com.zxw.controller;

import com.zxw.entity.Order;
import com.zxw.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * @author xiaoyu
 */
@RestController
@RequestMapping("/order")
@SuppressWarnings("all")
public class OrderController {

    @Autowired
    private OrderService orderService;


//    @ApiOperation(value = "订单支付接口（注意这里模拟的是创建订单并进行支付扣减库存等操作）")

    @PostMapping("/generateOrder")
    public ResponseEntity generateOrder(@RequestBody Order order) {
        orderService.insertOrder(1, 1, 1);
        return ResponseEntity.ok(null);
    }

    @PostMapping(value = "/orderPay")
    public ResponseEntity orderPay(@RequestParam(value = "count") Integer count,
                                   @RequestParam(value = "amount") BigDecimal amount) {
        return ResponseEntity.ok(orderService.orderPay(count, amount));

    }

    @PostMapping(value = "/mockInventoryWithTryException")
//    @ApiOperation(value = "模拟下单付款操作在try阶段异常，此时账户系统和订单状态会回滚，达到数据的一致性（注意:这里模拟的是系统异常，或者rpc异常）")
    public ResponseEntity mockInventoryWithTryException(@RequestParam(value = "count") Integer count,
                                                        @RequestParam(value = "amount") BigDecimal amount) {
        return ResponseEntity.ok(orderService.mockInventoryWithTryException(count, amount));
    }

    @PostMapping(value = "/mockInventoryWithTryTimeout")
//    @ApiOperation(value = "模拟下单付款操作在try阶段超时异常，此时账户系统和订单状态会回滚，达到数据的一致性（异常指的是超时异常）")
    public ResponseEntity mockInventoryWithTryTimeout(@RequestParam(value = "count") Integer count,
                                                      @RequestParam(value = "amount") BigDecimal amount) {
        return ResponseEntity.ok(orderService.mockInventoryWithTryTimeout(count, amount));
    }

}
