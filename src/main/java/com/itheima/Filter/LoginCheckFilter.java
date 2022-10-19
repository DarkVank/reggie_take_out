package com.itheima.Filter;


import com.alibaba.fastjson.JSON;
import com.itheima.common.BaseContext;
import com.itheima.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
       //
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1、定义不需要拦截的请求
        String[] urls = {
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg",
                "/user/login"
        };

        //2、进行路径匹配
        Boolean check = check(urls, request.getRequestURI());
        if(check){

            filterChain.doFilter(request,response);
            return;
        }
        log.info("拦截路径{}",request.getRequestURI());

        //3、判断是否登录
        if(request.getSession().getAttribute("employee") != null){

            Long id = (Long) request.getSession().getAttribute("employee");
            log.info("登录用户的id:{}",id);
            //存储当前用户id
            BaseContext.setCurrentId(id);

            filterChain.doFilter(request,response);
            return;
        }

        //3.1、判断是否移动端是否登录
        if(request.getSession().getAttribute("user") != null){

            Long id = (Long) request.getSession().getAttribute("user");
            log.info("登录用户的id:{}",id);
            //存储当前用户id
            BaseContext.setCurrentId(id);

            filterChain.doFilter(request,response);
            return;
        }


        //4、未登录
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return ;




    }

    /**
     * 判断请求是否放行
     * @param urls
     * @param requestURI
     * @return
     */
    public Boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
