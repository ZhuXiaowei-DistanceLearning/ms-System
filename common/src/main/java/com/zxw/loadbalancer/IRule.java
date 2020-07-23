package com.zxw.loadbalancer;

/**
 * @author zxw
 * @date 2020/7/23 16:10
 */
public interface IRule {
    int choose(Object var1);
}
