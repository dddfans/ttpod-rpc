package com.ttpod.rpc.server;

/**
 * date: 14-2-17 下午2:49
 *
 * @author: yangyang.cong@ttpod.com
 */
public abstract class AbstractServerProcessor implements ServerProcessor {

    @Override
    public String toString() {
        return description();
    }

    @Override
    public String description() {
        return getClass().getName();
    }
}