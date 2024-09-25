package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
/*
 * 自定义注解，用来标识某个方法需要进行功能字段自动填充处理
 */
//指定改注解放在方法的上方
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill{
    //需要指定属性，当前数据库的操作类型，可以通过枚举的方式
    //操作类型：UPDATE INSERT
    OperationType value();
}
