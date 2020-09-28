package com.caihao.ssoclient1.controller;

import com.caihao.ssoclient1.filter.AuthenticationFilter;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * create by caihao on 2020/9/28
 */
@Controller
public class SystemController {

    /**
     * 退出
     *
     * @param token 令牌，为null时表示用户是率先通过该系统进行退出的；不为null表示是由sso-server调用的退出
     * @param request 请求
     * @param response 响应
     * @return java.lang.String
     * @date 2020/9/28 14:56
     * @since 1.0.0
     */
    @GetMapping("/logout")
    @ResponseBody
    @SuppressWarnings("Duplicates")
    public String logout(String token, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 可以设置一个系统首页，当用户是访问的该系统退出时，退出后返回该系统首页
        String returnUrl = "http://192.168.237.23:8081/order/list";
        if (StringUtils.isEmpty(token)) {
            // 说明是用户主动退出，那么重定向到sso-server进行退出
            token = AuthenticationFilter.sessionIdTokenMap.get(request.getSession().getId());
            if (StringUtils.isEmpty(token)) {
                return "请先登录";
            } else {
                response.sendRedirect("http://192.168.231.1:8080/logout?returnUrl=" + returnUrl +
                        "&token=" + token);
                return "重定向到sso-server退出";
            }
        }
        // 说明是sso-server调用退出
        HttpSession httpSession = AuthenticationFilter.tokenSessionMap.get(token);
        if (httpSession != null) {
            // 清除map中的用户信息
            AuthenticationFilter.sessionIdTokenMap.remove(httpSession.getId());
            AuthenticationFilter.tokenSessionMap.remove(token);
            // 清除session
            httpSession.invalidate();
            return "client1 退出成功";
        }
        return "请先登录";
    }

}
