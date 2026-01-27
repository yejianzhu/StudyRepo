package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 * 异常信息在这里返回给前端
 */
@RestControllerAdvice//该注解是springBoot提供的全局统一处理注解,针对所有标注了@RestController的控制器,实现全局异常捕获、全局数据绑定、全局数据预处理
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获(业务异常)
     * @param ex
     * @return
     */
    @ExceptionHandler//该注解为异常处理注解,标记一个方法为异常处理方法,并指定该方法要捕获和处理的异常类型
    public Result exceptionHandler(BaseException ex) {
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());//给前端返回异常信息
    }

    /**
     * 添加员工异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        //SQLIntegrityConstraintViolationException:数据库完整性约束异常,根据日志输出的错误信息,获取需要的异常,不需要再写代码去判断是否违反完整性约束
        //异常信息:Duplicate entry 'zhangsan' for key 'employee.idx_username'
        //获取异常信息
        String message = ex.getMessage();
        if (message.contains("Duplicate entry")) {
            String[] split = message.split(" ");
            String username = split[2];
            String msg = username + MessageConstant.ALREADY_EXISTS;
            return Result.error(msg);
        }else{
            return Result.error(MessageConstant.UNKNOWN_ERROR);//给前端返回异常信息
        }
    }
}
