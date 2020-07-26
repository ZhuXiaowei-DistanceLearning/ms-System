package com.zxw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zxw.pojo.ProductPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author zxw
 * @date 2019/9/3 15:22
 */
@Mapper
public interface ProductMapper extends BaseMapper<ProductPo> {

    @Select("select * from t_product where id = ${productId}")
    ProductPo findById(@Param("productId") Long productId);

    @Update("update t_product set stock = stock - ${quantity},version = ${version}+1 where id = ${id} and version = ${version}")
    int decr(@Param("quantity") Integer quantity, @Param("version") Integer version, @Param("id") Long id);

}
