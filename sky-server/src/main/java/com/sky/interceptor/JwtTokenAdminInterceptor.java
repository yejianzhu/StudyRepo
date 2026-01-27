package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * jwt令牌校验的拦截器
 */
@Component//将该拦截器加入IOC容器
@Slf4j//生成日志对象
//实现SpringMVC的HandlerInterceptor接口
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;//获取令牌密钥令牌名称

    /**
     * 拦截器校验jwt
     * @param request 请求对象,用于获取请求头,参数信息等
     * @param response 响应对象,用于设置响应状态码,响应数据等
     * @param handler 被拦截的目标对象,即要访问的哪些资源放在这个对象中
     * @return
     * @throws Exception
     */
    //preHandle访问资源前调用该方法
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他静态资源
        //HandlerMethod:springMVC将controller层的方法打包到该类中
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是Controller的方法，直接放行
            return true;
        }

        //获取请求路径(URI:资源访问路径,不包含协议和端口)
        String requestURI = request.getRequestURI();
        //判断是否是登录请求/login,是登录操作,放行
        if (requestURI.contains("/login")) {
            log.info("登录请求,放行");
            return true;
        }

        //如果是访问Controller层的方法,执行下面的代码
        //根据yml的令牌名称从请求头中获取jwt令牌
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        //校验令牌
        try {
            log.info("jwt校验:{}", token);
            //根据yml的密钥和获取到的jwt令牌获取有效载荷的信息
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            //valueOf方法将字符串或long类型转化为Long类型,获取员工id
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
            log.info("当前员工id：{}", empId);
            //使用threadLocal来设置当前线程的局部变量
            //将员工id放到当前线程中,方便后续的使用
            BaseContext.setCurrentId(empId);
            //通过，放行
            return true;
        } catch (Exception ex) {
            //不通过,响应401状态码
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    /**
     * afterCompletion方法是在整个请求已经处理完毕并且完成响应后执行,进行收尾工作
     * 这里主要用来释放ThreadLocal的内存,防止当前线程收回线程池后,该数据还在,造成内存泄漏
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //释放ThreadLocal的内存,
        BaseContext.removeCurrentId();
    }
}
