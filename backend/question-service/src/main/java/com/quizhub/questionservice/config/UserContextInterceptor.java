package com.quizhub.questionservice.config;

import com.quizhub.questionservice.security.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class UserContextInterceptor implements HandlerInterceptor {

    private final UserContext userContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-Username");
        String roles = request.getHeader("X-Roles");

        if (userId != null) {
            userContext.setUserId(userId);
        }
        if (username != null) {
            userContext.setUsername(username);
        }
        if (roles != null) {
            userContext.setRoles(roles);
        }

        return true;
    }
}
