package com.caihao.ssoclient2.filter;

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
 * 这里只拦截了 /goods/* 接口，可以根据实际情况进行修改
 * create by caihao on 2020/9/25
 */
@WebFilter(filterName = "authenticationFilter", urlPatterns = "/goods/*")
@Slf4j
public class AuthenticationFilter implements Filter {

    @Autowired
    private RestTemplate restTemplate;

    // 用来绑定token和session的关系，退出的时候可以通过token将对应的session移除（sso-server调用退出接口时，通过token来移除session）
    public static ConcurrentHashMap<String, HttpSession> tokenSessionMap = new ConcurrentHashMap<>();
    // 用来绑定sessionId和token的关系，用于退出时，通过sessionId获取token（用户调用该系统的退出接口时需要通过该map来获取token）
    public static ConcurrentHashMap<String, String> sessionIdTokenMap = new ConcurrentHashMap<>();

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
        Object isLogin = session.getAttribute("isLogin");
        if (isLogin != null && (boolean) isLogin) {
            // 说明浏览器已经登录过，直接放行
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        // 看有没有携带token过来，如果携带了token的话，说明可能是sso-server返回的
        String token = request.getParameter("token");
        if (!StringUtils.isEmpty(token)) {
            // token 不为空，去验证token，同时带上系统退出url
            ResponseEntity<String> res = restTemplate.getForEntity("http://192.168.231.1:8080/verifyToken?token=" +
                    token + "&systemUrl=http://192.168.237.24:8082/logout", String.class);
            String flag = res.getBody();
            if (!StringUtils.isEmpty(flag)) {
                // 说明token验证成功，可以与客户端浏览器建立连接
                session.setAttribute("isLogin", true);
                // 将session和token关联起来，用于退出
                log.info("与客户端浏览器的sessionId：{}", session.getId());
                tokenSessionMap.put(token, session);
                sessionIdTokenMap.put(session.getId(), token);
                // 登录成功后可以选择直接放行
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
        }
        // 确实没有登录过的话，就重定向到sso-server认证中心验证
        response.sendRedirect("http://192.168.231.1:8080/toLogin?returnUrl=" + request.getRequestURL());
    }

    @Override
    public void destroy() {
        log.info("销毁");
    }
}
