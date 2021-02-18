package com.seckill.springbootseckill.annotation;

import java.lang.annotation.*;

/**
 * @author 迪达勒斯
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ServiceLock {
    String value() default "";
}
