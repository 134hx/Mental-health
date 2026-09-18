package com.example.xinli.util;

import com.example.xinli.common.TokenManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("token");
        // ✅ 先判断token为null，直接返回未登录，不要传给ConcurrentHashMap.get(null)
        if(token == null){
            response.setContentType("application/json;charset=utf-8");
            String json = "{\"code\":400,\"msg\":\"请先登录\",\"data\":null}";
            response.getWriter().write(json);
            return false;
        }
        Long userId = TokenManager.getUserId(token);
        if (userId == null) {
            response.setContentType("application/json;charset=utf-8");
            String json = "{\"code\":400,\"msg\":\"请先登录\",\"data\":null}";
            response.getWriter().write(json);
            return false;
        }
        request.setAttribute("loginUserId", userId);
        return true;
    }
}


