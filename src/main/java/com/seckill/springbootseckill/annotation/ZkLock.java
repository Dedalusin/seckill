package com.seckill.springbootseckill.annotation;

import java.lang.annotation.*;

/**
 * @author 迪达勒斯
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ZkLock {
    String value() default "";
}
