package com.seven.chat.interceptor;

import com.seven.chat.domain.SystemConstant;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @description: 定义登录拦截器，还未添加到容器中，可以在自定义配置文件里进行添加，注册一个拦截器
 * @author: Seven
 * @Date: 2019/05/16 22:27
 */
@Component
public class LoginHandlerInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession();

        String sid = (String) session.getAttribute("sid");

        // 查看自己维护的session库中会话是否还存在（就算项目重启了，由于浏览器会缓存session信息，
        // 那么它提交的请求里肯定有用户信息，所以就不能做到拦截的效果，因为项目重启后存在内存中的对象都会释放，所以能达到项目重启后用户需要重新登录）
        // 或者我Listener里面做，当项目销毁时通知浏览器
//        session = (HttpSession) SystemConstant.SOCKET_SESSION.get(sid);

//        if ( session instanceof Object ) {
//            // 表示此用户已成功登录，则放行
//            return true;
//        }

        String loginUser =  (String) session.getAttribute("loginUser");

        if ( loginUser instanceof Object ) {
            // 表示此用户已成功登录，则放行
            return true;
        }

        request.setAttribute("msg", "没有权限，请先登录！");

        // 转发（这里不可以用重定向，因为我们要把信息带过去！重定向是重新发起一个请求）
        request.getRequestDispatcher("/").forward(request, response);

        return false;
    }
}