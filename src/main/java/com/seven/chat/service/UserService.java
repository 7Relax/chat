package com.seven.chat.service;

import com.seven.chat.dao.mapper.UserMapper;
import com.seven.chat.entity.User;
import com.seven.chat.utils.UUIDTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Description:
 * @Author: Seven
 * @Date: 2019/06/23 20:25
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User getUserByUsername(String username) {

        return userMapper.getUserByUsername(username);

    }

    public Integer addUser(String username, String password) {
        User user = new User();
        user.setId(UUIDTool.getUUID());
        user.setUsername(username);
        user.setPassword(password);
        user.setGender("1");            // 默认是男生
        user.setRole("2");              // 默认是普通用户
        user.setStatus("2");            // 默认离线
        user.setCreateTime(new Date());
        return userMapper.addUser(user);
    }

}