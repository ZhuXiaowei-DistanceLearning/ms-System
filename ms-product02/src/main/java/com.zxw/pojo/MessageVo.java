package com.zxw.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zxw
 * @date 2019/9/11 19:42
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageVo {
    private long userId;
    private long goodsId;
    private int quantity;
}
