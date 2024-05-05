package web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import web.Utils.UserHolder;
import web.global.GlobalVariables;

public class AuthenticationInterceptor implements HandlerInterceptor {
//    @Autowired
//    private GlobalVariables variables;
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
////        如果未登录，需要跳转到登录界面
//
//    }
}
