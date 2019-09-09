package com.zxw.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zxw
 * @date 2019/9/3 15:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_product")
public class ProductPo implements Serializable {
    private static final long serialVersionUID = 3288311147760635602L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private String productName;
    private int stock;
    private double price;
    private int version;
    private String note;

}
