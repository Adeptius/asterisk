package ua.adeptius.asterisk.interceptors;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AccessControlOriginInterceptor extends HandlerInterceptorAdapter {


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        String accessControlAllowOrigin = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Origin", accessControlAllowOrigin);
    }
}
