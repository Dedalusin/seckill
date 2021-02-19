package com.seckill.springbootseckill.annotation;

import java.lang.annotation.*;

/**
 * @author 迪达勒斯
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedServiceLock {
    String vale() default "用于在事务外层添加分布式锁byAOP";
}
