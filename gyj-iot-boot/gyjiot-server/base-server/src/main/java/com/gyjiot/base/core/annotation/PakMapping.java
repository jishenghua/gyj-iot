package com.gyjiot.base.core.annotation;

import java.lang.annotation.*;

/**
 * 字段映射
 * @author shenghua.ji
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PakMapping {

    int[] types();

    String desc() default "";
}
