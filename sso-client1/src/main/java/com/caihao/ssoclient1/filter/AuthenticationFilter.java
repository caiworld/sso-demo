package com.caihao.ssoclient1.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * create by caihao on 2020/9/25
 */
@Slf4j
@WebFilter(filterName = "/authenticationFilter", urlPatterns = "/order/*")
public class AuthenticationFilter implements Filter {

    // 用来绑定token和session的关系，退出的时候可以通过token将对应的session移除（sso-server调用退出接口时，通过token来移除session）
    public static ConcurrentHashMap<String, HttpSession> tokenSessionMap = new ConcurrentHashMap<>();
    // 用来绑定sessionId和token的关系，用于退出时，通过sessionId获取token（用户调用该系统的退出接口时需要通过该map来获取token）
    public static ConcurrentHashMap<String, String> sessionIdTokenMap = new ConcurrentHashMap<>();

    @Autowired
    private RestTemplate restTemplate;

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
        Object obj = session.getAttribute("isLogin");
        if (obj != null && (boolean) obj) {
            // 说明浏览器已经登录过，直接放行
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        // 看有没有携带token过来，如果携带了token的话，说明可能是sso-server返回的
        String token = request.getParameter("token");
        if (!StringUtils.isEmpty(token)) {
            // 去sso-server认证中心校验，带上系统的退出地址
            ResponseEntity<String> res = restTemplate.getForEntity("http://192.168.231.1:8080/verifyToken?token=" +
                    token + "&systemUrl=http://192.168.237.23:8081/logout", String.class);
            String sessionId = res.getBody();
            if (!StringUtils.isEmpty(sessionId)) {
                // 说明登录成功，可以与客户端浏览器建立session
                session.setAttribute("isLogin", true);
                log.info("客户端1与浏览器的sessionId：", session.getId());
                // 保存session和token的关系，用于将来退出登录
                tokenSessionMap.put(token, session);
                sessionIdTokenMap.put(session.getId(), token);
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
        }
        // 没有在系统1登录过的话，就重定向到sso-server认证中心认证
        response.sendRedirect("http://192.168.231.1:8080/toLogin?returnUrl=" + request.getRequestURL());
    }

    @Override
    public void destroy() {
        log.info("销毁");
    }
}
