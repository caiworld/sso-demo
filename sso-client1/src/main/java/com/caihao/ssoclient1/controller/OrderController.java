package com.caihao.ssoclient1.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * create by caihao on 2020/9/25
 */
@Controller
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @GetMapping("/list")
    @ResponseBody
    public String getOrderList(HttpServletRequest request) {
        log.info("client1的/list中sessionId：{}", request.getSession().getId());
        return "getOrderList success";
    }

}
