package com.inteliagent.auth.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Single instance limiter. Use a shared gateway limiter before scaling auth replicas. */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {
    private record Window(long start, int count) {}
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!request.getMethod().equals("POST") || !request.getRequestURI().startsWith("/api/user/")) {
            chain.doFilter(request, response);
            return;
        }
        long now = System.currentTimeMillis();
        windows.entrySet().removeIf(e -> now - e.getValue().start() > 60_000);
        String key = request.getRemoteAddr() + ":" + request.getRequestURI();
        if (windows.size() >= 10000 && !windows.containsKey(key)) {
            response.setStatus(429); response.setHeader("Retry-After", "60"); return;
        }
        var current = windows.compute(key, (k, old) -> old == null || now - old.start() > 60_000
                ? new Window(now, 1) : new Window(old.start(), old.count() + 1));
        String path = request.getRequestURI();
        boolean sessionMaintenance = path.equals("/api/user/refresh") || path.equals("/api/user/refresh/web")
            || path.equals("/api/user/logout") || path.equals("/api/user/logout/web");
        int limit = path.endsWith("sendEmail") ? 3 : sessionMaintenance ? 120 : 10;
        if (current.count() > limit || windows.size() > 10000) {
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.setContentType("application/problem+json");
            response.getWriter().write("{\"status\":429,\"detail\":\"Muitas tentativas. Aguarde um minuto.\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
