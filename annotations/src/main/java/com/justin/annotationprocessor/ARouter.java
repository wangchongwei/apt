package com.justin.annotationprocessor;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE) // 表明注解是添加在类上
@Retention(RetentionPolicy.CLASS) // 表明在编译器执行
public @interface ARouter {

    String path();

    String group() default "";

}