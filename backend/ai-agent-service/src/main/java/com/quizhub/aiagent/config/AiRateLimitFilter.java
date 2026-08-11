package com.quizhub.aiagent.config;

import com.quizhub.aiagent.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AiRateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (!path.startsWith("/api/v1/ai/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientId = resolveClientId(request);

        boolean allowed =
                rateLimitService.isAllowed(clientId);

        if (!allowed) {

            response.setStatus(429);
            response.setContentType("application/json");

            response.getWriter().write("""
                    {
                        "success": false,
                        "message": "AI request rate limit exceeded. Please try again later."
                    }
                    """);

            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientId(HttpServletRequest request) {

        String userId = request.getHeader("X-User-Id");

        if (userId != null && !userId.isBlank()) {
            return "user:" + userId;
        }

        String ip = request.getRemoteAddr();

        return "ip:" + ip;
    }
}