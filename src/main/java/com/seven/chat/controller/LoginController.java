package com.seven.chat.controller;

import com.seven.chat.dao.mapper.UserMapper;
import com.seven.chat.domain.SystemConstant;
import com.seven.chat.entity.User;
import com.seven.chat.utils.UUIDTool;
import com.seven.chat.websocket.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @Description:
 * @Author: Seven
 * @Date: 2019/06/22 11:32
 */
@Controller
public class LoginController {

    private Logger logger =  LoggerFactory.getLogger(getClass());

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WebSocketServer webSocketServer;

    @RequestMapping("/login")
    public String login(String username, String password, Map<String, Object> map, HttpServletRequest request, HttpSession httpSession) {
        logger.info("login >> username : " + username);
        // 参数合法性校验

        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            try {
                // 查询数据库
                User user = userMapper.getUserByUsername(username);

                // 查询到用户 且 密码校验正确
                if (user instanceof Object && password.equals(user.getPassword())) {

                    // 从map中先获取sid信息，若获取到了则说明此用户已经登录过了，可以将前者踢出了（单点登录）
                    String sid = SystemConstant.USER_SOCKET_MAP.get(username);
                    if (sid != null) {
                        // 登录过，则踢出
                        webSocketServer.sendInfo("code_force_logout", sid);
                    }
                    // 将 username 与 sid 绑定到 map 中
                    sid = UUIDTool.getUUID();
                    SystemConstant.USER_SOCKET_MAP.put(username, sid);

                    // 转发到导航界面
                    map.put("sid", sid);
                    map.put("loginUser", username);

                    // 也可以通过HttpServletRequest来传递
//                    request.setAttribute("loginUser", username);

                    // 登录成功后将用户信息存入HttpSession中，因为是转发所以也没必要通过HttpSession传递参数
//                    HttpSession session = request.getSession();
//                    session.setAttribute("loginUser", username);
                    return "main/navigation";
                }
            } catch (Exception e) {
                logger.error("login >> Exception : "+e);
            }
        }

        Object msg = request.getAttribute("msg");
        // msg == null 表示拦截器等其它地方转发到login的时候是没有携带信息的，那这里的msg就不能被覆盖了（要展示到页面去）
        if (msg == null) {
            map.put("msg", "账号或密码错误");
        }

        // 登录失败，转发到登录界面
        return "main/login";
    }

    /**
     * 退出登录
     * @param session
     * @return
     */
    @GetMapping("/logout")
    public String logOut(HttpSession session) {

        // 1、清空Session中的用户信息
        session.removeAttribute("loginUser");

        // 2、注销Session
        session.invalidate();

        // 3、返回登录页面，这里要用重定向，不让其转发
        return "redirect:/";
    }

}