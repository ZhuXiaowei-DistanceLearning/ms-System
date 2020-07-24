package com.zxw.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * @author zxw
 * @date 2020/7/24 9:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_purchase_record")
public class OrderDO {
    private static final long serialVersionUID = 328831114776063574L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long user_id;
    private Long product_id;
    private double price;
    private int quantity;
    private double sum;
    private Timestamp purchase_date;
    private String note;
}
