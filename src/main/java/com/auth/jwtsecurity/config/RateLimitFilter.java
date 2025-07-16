package com.auth.jwtsecurity.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitFilter implements Filter {

    private static final int LIMIT = 100; // Max requests per minute per IP
    private static final long TIME_WINDOW_MS = 60 * 1000; // 1 minute

    private final Map<String, RequestInfo> ipRequestMap = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String ip = httpRequest.getRemoteAddr();
        long currentTime = System.currentTimeMillis();

        ipRequestMap.compute(ip, (key, info) -> {
            if (info == null || currentTime - info.startTime > TIME_WINDOW_MS) {
                return new RequestInfo(1, currentTime);
            } else {
                info.requestCount++;
                return info;
            }
        });

        RequestInfo info = ipRequestMap.get(ip);
        if (info.requestCount > LIMIT) {
            httpResponse.setStatus(429);
            httpResponse.getWriter().write("Too many requests. Please try again later.");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // Optional
    }

    @Override
    public void destroy() {
        // Optional
    }

    private static class RequestInfo {
        int requestCount;
        long startTime;

        RequestInfo(int requestCount, long startTime) {
            this.requestCount = requestCount;
            this.startTime = startTime;
        }
    }
}
