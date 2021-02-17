package com.seckill.springbootseckill.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ServiceLock {
    String value() default "";
}
