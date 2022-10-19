package com.itheima.controller;

import com.itheima.common.R;
import com.itheima.entity.User;
import com.itheima.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    /**
     * 获取验证码
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user , HttpSession session){


        //获取手机号
        String phone = user.getPhone();
        log.info("登录手机号：{}",phone);

        if(StringUtils.isNotEmpty(phone)){
            //生成验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info(code);

            //session中存储验证码
            session.setAttribute(phone,code);

            return R.success("获取成功");
        }
        return R.error("验证码获取失败");
    }
}
