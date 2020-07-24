package com.zxw.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author zxw
 * @date 2019/9/3 15:15
 */
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
    private Date beginTime;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getBeginTime() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = sdf.format(beginTime);
        Timestamp value = Timestamp.valueOf(sdf.format(beginTime));
        Date date = sdf.parse(format);
        return  date;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }
}
