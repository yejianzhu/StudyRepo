package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解,标识那些方法需要自动填充字段
 */
@Target(ElementType.METHOD)//指定该注解的作用范围在方法上
@Retention(RetentionPolicy.RUNTIME)//指定该注解的生命周期到运行时
public @interface AutoFill {
    OperationType value();//该注解的属性,标识对数据库是insert操作还是update操作
}
