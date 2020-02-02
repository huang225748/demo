package com.itmuch.usercenter.auth;

import com.itmuch.usercenter.util.JwtOperator;
import io.jsonwebtoken.Claims;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Objects;

@Aspect
@Component
public class AuthAspect {

    @Autowired
    private JwtOperator jwtOperator;

    @Around("@annotation(com.itmuch.usercenter.auth.CheckLogin)")
    public Object checkLogin(ProceedingJoinPoint point) throws Throwable {
            checkToken();
            return point.proceed();
    }

    private void checkToken() {
        try {
            //1.从header里面获取token
            HttpServletRequest request = getHttpServletRequest();

            String token = request.getHeader("X-Token");
            //2.校验token是否合法,如果不合法,直接抛异常;如果合法放行
            Boolean isvalid = jwtOperator.validateToken(token);
            if (!isvalid) {
                throw new SecurityException("Token不合法!");
            }
            //3.如果校验成功,那么就将用户的信息设置到request的attribute里面
            Claims claims = jwtOperator.getClaimsFromToken(token);

            request.setAttribute("id", claims.get("id"));
            request.setAttribute("wxNickname", claims.get("wxNickname"));
            request.setAttribute("role", claims.get("role"));
        } catch (Throwable throwable) {
            throw new SecurityException("用户无权访问!",throwable);
        }
    }

    private HttpServletRequest getHttpServletRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
        return attributes.getRequest();
    }

    @Around("@annotation(com.itmuch.usercenter.auth.CheckAuthorization)")
    public Object CheckAuthorization(ProceedingJoinPoint point) throws Throwable {
        try {
        //1.验证token是否合法
        this.checkToken();
        //2.验证用户角色是否匹配
        HttpServletRequest request = getHttpServletRequest();
        String role = (String)request.getAttribute("role");

        MethodSignature signature = (MethodSignature)point.getSignature();
        Method method = signature.getMethod();
        CheckAuthorization annotation = method.getAnnotation(CheckAuthorization.class);

        String value = annotation.value();
        if(!Objects.equals(role, value)){
            throw new SecurityException("用户无权访问!");
        }

        } catch (Throwable throwable) {
            throw new SecurityException("用户无权访问!",throwable);
        }
        return point.proceed();
    }
}
