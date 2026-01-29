package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 该类为实现公共字段自动填充的切面类
 */
@Slf4j
@Aspect//标识该类的切面类
@Component//将该切面类加入IOC容器
public class AutoFillAspect {
    /**
     * 切入点
     * autoFillPointCut方法为空方法,要被后面的通知方法引用
     * Pointcut注解里面写切入点表达式,execution表示拦截那些包下的方法,@annotation表示拦截标识了指定注解的方法
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }


    /**
     * 前置通知,在该通知方法中对公共字段进行填充
     *
     * @param joinPoint// 连接点对象,包含被拦截方法的信息,比如方法签名(方法签名包含方法的信息)
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段自动填充......");
        //获取到当前被拦截的方法上的数据库操作类型
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();//方法签名对象,包含目标方法的信息,org.aspectj.lang.reflect包下的,别导错包
        AutoFill autoFill = methodSignature.getMethod().getAnnotation(AutoFill.class);//获取目标方法上的注解对象
        OperationType operationType = autoFill.value();//获取该注解的值
        //获取到当前被拦截的方法上的参数--实体对象
        Object[] args = joinPoint.getArgs();//获取目标方法上的所有参数,约定好实体放在第一个位置,后面就获取第一个位置的参数
        if (args == null || args.length == 0) {
            //判断参数是否为空,一般情况不会为空,这里为保险起见,做一下判断
            return;
        }
        Object entity = args[0];//取第一个参数
        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        //根据当前不同的数据库操作类型,为对应的属性通过反射赋值
        if (operationType == OperationType.INSERT) {
            //插入操作
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (operationType == OperationType.UPDATE) {
            //修改操作
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
