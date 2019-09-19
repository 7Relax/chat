package com.seven.chat.interceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 全局参数返回处理
 */
public class BasePathInterceptor implements HandlerInterceptor {
    /**
     * 是否开启HTTP请求中的数据加密传输开关，告诉前台是否开启请求响应数据的加解密
     * <br>0：不加密 1：加密
     */
//    @Value("${HTTPRequestDataEncryption}")
    private String HTTPRequestDataEncryption = "1";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int port = request.getServerPort();
        String path = request.getContextPath();
        String basePath = "";
        if (port == 80) {
            basePath = scheme + "://" + serverName + path + "/";
        } else {
            basePath = scheme + "://" + serverName + ":" + port + path + "/";
        }
        request.setAttribute("basePath", basePath);
        /**
         * 如果配置是否开启AES加密开关不正确则按照默认开启
         */
        if (HTTPRequestDataEncryption == null || HTTPRequestDataEncryption.trim().equalsIgnoreCase("") || !(HTTPRequestDataEncryption.trim().equalsIgnoreCase("1") || HTTPRequestDataEncryption.trim().equalsIgnoreCase("0"))) {
            HTTPRequestDataEncryption = "1";
        }
        request.setAttribute("encryptionSwitch", HTTPRequestDataEncryption);
        return true;
    }

}