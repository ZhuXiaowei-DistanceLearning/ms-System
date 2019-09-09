package com.zxw.exception;

import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * @author zxw
 * @date 2019/9/8 21:39
 */
public class ExceptionUtil {

    public static void handleException(BlockException ex) {
        System.out.println("Oops: " + ex.getClass().getCanonicalName());
    }
}
