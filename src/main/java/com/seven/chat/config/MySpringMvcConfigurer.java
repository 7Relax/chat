package com.seven.chat.config;

import com.seven.chat.interceptor.BasePathInterceptor;
import com.seven.chat.interceptor.LoginHandlerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @description:
 * @author: Seven
 * @date: 2019/06/22 10:30
 * @version:
 */
@Configuration
public class MySpringMvcConfigurer {

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer(){
            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                // 请求：   /   或者  /index  或者  /login
                registry.addViewController("/").setViewName("main/login");
                registry.addViewController("/index").setViewName("main/login");
                registry.addViewController("/login").setViewName("main/login");

                // 登录成功后（账户密码验证）会重定向到 /chat（get请求：http://localhost:8080/chat/chat）
                // 相当于重新发起一个请求，再经过这里后转到 main/chat.html 页面，这个页面就是登录后的聊天界面
                registry.addViewController("/chat").setViewName("main/chat");

                // 注册页面
                registry.addViewController("/toRegistry").setViewName("main/registry");
            }

            @Override
            public void addInterceptors(InterceptorRegistry registry) {

                // 拦截器注册器 registry 来注册一个拦截器
                registry.addInterceptor(new LoginHandlerInterceptor())
                        // 指定要拦截的请求，/** 表示拦截所有请求
                        .addPathPatterns("/**")
                        // 排除不需要拦截的请求
                        .excludePathPatterns("/", "/index", "/login", "/logout", "/toRegistry", "/registry")
                        .excludePathPatterns("/privateOrPublicRoom/*")
                        // Springboot2+ 之后需要将静态资源文件访问路径给排除，而Springboot1.5以前帮我们做了这个操作
                        .excludePathPatterns("/favicon.ico","/css/*", "/images/*", "/js/**");

                registry.addInterceptor(new BasePathInterceptor()).addPathPatterns("/**");
            }

//            @Override
//            public void addResourceHandlers(ResourceHandlerRegistry registry) {
//                registry.addResourceHandler("/static/**").
//                        addResourceLocations("classpath:/static/");
//            }


        };
    }

//    @Bean
//    public LocaleResolver localeResolver() {
//        return new MyLocaleResolver();
//    }
//
//    @Bean
//    public DefaultErrorAttributes errorAttributes() {
//        return new MyErrorAttributes();
//    }

}