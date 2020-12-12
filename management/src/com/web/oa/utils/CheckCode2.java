package com.web.oa.utils;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
//验证码
public class CheckCode2 extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(120, 40,4,100);
        // 取出 验证码中 字符

        // 存在session 域中

        //lineCaptcha.getCode();
        HttpSession session = req.getSession();
        
        session.setAttribute("randomCode",lineCaptcha.getCode());
        
        lineCaptcha.write(resp.getOutputStream());

        // 将验证码响应给浏览器





    }
}
