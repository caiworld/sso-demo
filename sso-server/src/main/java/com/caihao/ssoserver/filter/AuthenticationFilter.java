package com.caihao.ssoserver.filter;

import com.caihao.ssoserver.controller.LoginController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * create by caihao on 2020/9/24
 */
@WebFilter(filterName = "authenticationFilter", urlPatterns = "/toLogin")
@Slf4j
public class AuthenticationFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("初始化");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession();
        log.info("认证中心的doFilter中sessionId：{}", session.getId());
        Object obj = session.getAttribute("isLogin");
        String returnUrl = request.getParameter("returnUrl");
        if (obj != null && ((boolean) obj)) {
            // 已经登录了的话，就将token返回
            if (returnUrl.contains("?")) {
                returnUrl = returnUrl + "&token=" + LoginController.sessionTokenMap.get(session.getId());
            } else {
                returnUrl = returnUrl + "?token=" + LoginController.sessionTokenMap.get(session.getId());
            }
            response.sendRedirect(returnUrl);
            return;
        }
        // 没有登录，重定向到登录页面
        String toLoginUrl = "/toLoginView";
        if (!StringUtils.isEmpty(returnUrl)) {
            toLoginUrl = toLoginUrl + "?returnUrl=" + returnUrl;
        }
        response.sendRedirect(toLoginUrl);
    }

    @Override
    public void destroy() {
        log.info("销毁");
    }
}
