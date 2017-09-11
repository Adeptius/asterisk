package ua.adeptius.asterisk.interceptors;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import ua.adeptius.asterisk.test.Options;

import javax.interceptor.Interceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class AccessControlOriginInterceptor extends HandlerInterceptorAdapter {

    private Options options;

    @Autowired
    public void setOptions(Options options) {
        this.options = options;
    }

    public static boolean profiling;

    public static HashMap<String, Long> map = new HashMap<>();

    private long startTime;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String accessControlAllowOrigin = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Origin", accessControlAllowOrigin);
        if (options.isProfilingEnabled()) {
            startTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime());
        }
        return super.preHandle(request, response, handler);
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (options.isProfilingEnabled()){
            String path = request.getServletPath();
            long now = TimeUnit.NANOSECONDS.toMicros(System.nanoTime());
            long past = now - startTime;
            Long time = map.get(path);
            if (time == null){
                map.put(path, 0L);
                time = 0L;
            }
            time += past;
            map.put(path, time);
        }
        super.postHandle(request, response, handler, modelAndView);
    }


    public static void printProfiling(){
        map.forEach((s, aLong) -> System.out.println(s + " - " + TimeUnit.MICROSECONDS.toMillis(aLong)));
    }
}
