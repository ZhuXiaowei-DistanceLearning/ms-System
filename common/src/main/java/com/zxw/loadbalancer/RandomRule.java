package com.zxw.loadbalancer;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author zxw
 * @date 2020/7/23 16:12
 */
public class RandomRule extends AbstractLoanBalancerRule {
    @Override
    public int choose(Object var1) {
        return this.chooseRandomInt((int)var1);
    }

    public int chooseRandomInt(int keysCount) {
        return ThreadLocalRandom.current().nextInt(keysCount);
    }
}
