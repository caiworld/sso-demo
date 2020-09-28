package com.caihao.ssoclient2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * create by caihao on 2020/9/25
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {

    /**
     * 系统2中需要登录才能访问的某资源
     *
     * @return java.lang.String
     * @date 2020/9/28 15:00
     * @since 1.0.0
     */
    @GetMapping("/list")
    @ResponseBody
    public String getGoodsList() {
        return "sso-client2 getGoodsList success";
    }

}
