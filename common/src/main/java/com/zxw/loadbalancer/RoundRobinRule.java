package com.zxw.loadbalancer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zxw
 * @date 2020/7/23 16:12
 */
public class RoundRobinRule extends AbstractLoanBalancerRule {
    private static AtomicInteger nextServerCyclicCounter = new AtomicInteger(0);

    @Override
    public int choose(Object var1) {
        return this.incrementAndGetModulo((int)var1);
    }

    public int incrementAndGetModulo(int modulo) {
        int current;
        int next;
        do {
            current = this.nextServerCyclicCounter.get();
            next = (current + 1) % modulo;
        } while (!this.nextServerCyclicCounter.compareAndSet(current, next));
        return next;
    }
}
