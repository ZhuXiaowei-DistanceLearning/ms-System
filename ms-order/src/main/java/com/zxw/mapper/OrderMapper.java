package com.zxw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zxw.pojo.OrderDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zxw
 * @date 2020/7/24 9:43
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderDO> {
}
