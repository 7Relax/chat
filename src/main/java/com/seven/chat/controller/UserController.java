package com.seven.chat.controller;

import com.seven.chat.domain.JsonResponse;
import com.seven.chat.entity.User;
import com.seven.chat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Description:
 * @Author: Seven
 * @Date: 2019/06/23 20:13
 */
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 新增一个用户（注册）
     * @param params
     * @return
     */
    @PostMapping("/registry")
    @ResponseBody
    public Map<String, Object> addUser(@RequestBody Map<String, Object> params) {
        // 入口参数校验
        String username = (String) params.get("username");
        String password = (String) params.get("password");

        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            // 判断用户名是否已注册
            User user = userService.getUserByUsername(username);
            if (user instanceof Object) {
                // 此用户已注册
                return JsonResponse.buildFailure("此用户名已被占用");
            } else {
                // 注册新用户
                Integer addResult = userService.addUser(username, password);
                if (addResult instanceof Object) {
                    // 创建成功
                    return JsonResponse.buildSuccess();
                }
            }
        }

        // 注册失败
        return JsonResponse.buildFailure("注册出现异常！");
    }

}