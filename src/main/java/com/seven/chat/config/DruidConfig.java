package com.seven.chat.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 绑定Druid相关信息
 * @Author: Seven
 * @Date: 2019/06/21 20:21
 */
@Configuration
public class DruidConfig {

    @ConfigurationProperties(prefix = "spring.datasource")
    @Bean
    public DataSource dataSource() {
        return new DruidDataSource();
    }

    /**
     * 配置Druid监控
     * 1. 配置一个管理后台的Servlet
     * 2. 配置一个监控的filter
     */
    // 1. 配置一个管理后台的Servlet
    @Bean
    public ServletRegistrationBean statViewServlet() {
        // 注意是 /druid/* 这个路径就会映射到StatViewServlet这个Servlet来
        ServletRegistrationBean bean = new ServletRegistrationBean<>(new StatViewServlet(), "/druid/*");
        // 设置初始化参数
        Map<String, String> initParameters = new HashMap<>();
        initParameters.put(StatViewServlet.PARAM_NAME_USERNAME, "root");
        initParameters.put(StatViewServlet.PARAM_NAME_PASSWORD, "123");
        // 如果不写则默认所有ip都可以访问
        initParameters.put(StatViewServlet.PARAM_NAME_ALLOW, "");
        initParameters.put(StatViewServlet.PARAM_NAME_DENY, "192.168.1.1");

        bean.setInitParameters(initParameters);
        return bean;
    }

    // 2. 配置一个监控的filter
    @Bean
    public FilterRegistrationBean webStatFilter() {
//        FilterRegistrationBean<WebStatFilter> bean = new FilterRegistrationBean<>(new WebStatFilter());
        FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new WebStatFilter());
        // 设置初始化参数
        Map<String, String> initParameters = new HashMap<>();
        initParameters.put(WebStatFilter.PARAM_NAME_EXCLUSIONS, "*.js, *.css, /druid/*");
        bean.setInitParameters(initParameters);
        // 设置拦截请求
        bean.setUrlPatterns(Arrays.asList("/*"));
        return bean;
    }

}



