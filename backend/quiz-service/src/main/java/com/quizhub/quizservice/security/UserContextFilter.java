package com.quizhub.quizservice.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class UserContextFilter implements Filter {

    private final UserContext userContext;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String userIdHeader = httpRequest.getHeader("X-User-Id");
        String usernameHeader = httpRequest.getHeader("X-Username");
        String rolesHeader = httpRequest.getHeader("X-Roles");

        if (userIdHeader != null && !userIdHeader.isBlank()) {
            userContext.setUserId(userIdHeader);
        }
        if (usernameHeader != null) {
            userContext.setUsername(usernameHeader);
        }
        if (rolesHeader != null) {
            userContext.setRoles(rolesHeader);
        }

        chain.doFilter(request, response);
    }
}
