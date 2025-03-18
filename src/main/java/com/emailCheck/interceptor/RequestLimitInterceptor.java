package com.emailCheck.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RequestLimitInterceptor implements HandlerInterceptor {

    private final ConcurrentHashMap<String, Integer> requestCounts = new ConcurrentHashMap<>();
    private final int FREE_LIMIT = 3;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            String clientIp = request.getRemoteAddr();
            int count = requestCounts.getOrDefault(clientIp, 0);

            if (count >= FREE_LIMIT) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Limite de requisições grátis atingido. Faça login para continuar.");
                return false;
            }

            requestCounts.put(clientIp, count + 1);
        }

        return true;
    }
}