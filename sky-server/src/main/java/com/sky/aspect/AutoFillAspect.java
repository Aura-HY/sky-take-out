package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Slf4j
@Component
//TODO 是怎么通过那个自定义注解AutoFill调用到这个类里的函数的
//TODO 自定义注解是如何调用/使用？
public class AutoFillAspect {
    /**
     * 切入点
     */
    //在 com.sky.mapper 包下的任意类中，任何带有 @AutoFill 注解的方法都会被拦截
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    /**
     * 前置通知，在通知中进行公共字段的赋值
     */
    //表明beforeAutoFill方法是一个前置增强，并且它应该应用于autoFillPointCut()切点所匹配的所有方法
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        //参数为连接点
        log.info("开始进行公共字段自动填充...");

        //获取当前被拦截的方法上的数据库操作类型
        //AOP编程+反射
        //TODO 前面两行的代码的作用是什么？为什么不能只用最后一个？
        //TODO 向下转型的语法，AutoFill.class

        //joinPoint.getSignature()获取连接点（即当前被拦截的方法）的签名对象，包含了方法的元信息，比如方法名称、参数类型、返回值类型等。
        // 通过向下转型为 MethodSignature，我们能够获取特定于方法的信息。
        MethodSignature signature =(MethodSignature) joinPoint.getSignature();//方法签名对象

        //通过 MethodSignature 获取当前方法对象，并调用 getAnnotation() 方法来获取方法上的 @AutoFill 注解实例
        //AutoFill.class 语法表示 AutoFill 注解的 类对象。这是 Java 反射的一部分，允许我们在运行时获取注解类型。
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获得方法上的注解对象

        OperationType operationType = autoFill.value();//获得数据库的操作类型

        //获取当前被拦截方法的参数数组--这个项目中都是封装成了实体对象
        Object[] args = joinPoint.getArgs();
        //避免在尝试访问数组元素时出现NullPointerException或数组越界异常。
        if(args == null ||args.length==0){
            return;
        }
        Object entity=args[0];

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //根据不同操作类型，为对应的属性通过反射来赋值
        if(operationType==OperationType.INSERT){
            //为4个公共字段赋值
            try{
                //TODO 为什么不能直接set，这种写法？
                //entity是一个对象，这里要用到反射，拿到这个对象的类才能用反射拿到类中的方法，方法接受一个后者类型的参数
                //然后将方法定义为常量，防止出错
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射为对象属性赋值
                setCreateUser.invoke(entity,currentId);
                setUpdateTime.invoke(entity,now);
                setCreateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
                //因为invoke会报异常
            }catch (Exception e){
                e.printStackTrace();
            }
        } else if (operationType == OperationType.UPDATE) {
            //为两个公共字段赋值
            try{
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射为对象属性赋值
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
                //因为invoke会报异常
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}