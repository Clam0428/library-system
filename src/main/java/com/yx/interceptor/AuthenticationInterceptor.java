package com.yx.interceptor;

import com.yx.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登录认证拦截器
 */
@Slf4j
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 需要放行的路径
     */
    private static final String[] ALLOW_URLS = {
            "/api/admin/login",
            "/api/reader/login",
            "/api/reader/register",
            "/api/notice/published",
            "/static/",
            "/error"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        // 检查是否是允许的路径
        if (isAllowUrl(requestURI)) {
            return true;
        }

        // 检查会话中是否有用户
        HttpSession session = request.getSession(false);
        Object admin = null;
        Object reader = null;

        if (session != null) {
            admin = session.getAttribute("admin");
            reader = session.getAttribute("reader");
        }

        // 如果没有登录，返回未授权
        if (admin == null && reader == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            ApiResponse<Void> apiResponse = ApiResponse.fail(401, "未登录，请先登录");
            response.getWriter().print(OBJECT_MAPPER.writeValueAsString(apiResponse));
            log.warn("未认证访问: {}", requestURI);
            return false;
        }

        log.debug("认证通过: {}", requestURI);
        return true;
    }

    /**
     * 检查URL是否允许访问
     */
    private boolean isAllowUrl(String requestURI) {
        for (String url : ALLOW_URLS) {
            if (requestURI.startsWith(url) || requestURI.equals(url)) {
                return true;
            }
        }
        return false;
    }
}
