package ua.adeptius.asterisk.interceptors;


import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.dao.Settings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Component
public class AccessControlOriginInterceptor extends HandlerInterceptorAdapter {

    public static HashMap<String, Long> map = new HashMap<>();
    private long startTime;
    private static Settings settings = Main.settings;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        if (settings.isProfilingEnabled()) {
            startTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime());
        }
        return super.preHandle(request, response, handler);
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (settings.isProfilingEnabled()){
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
        System.out.println();
        System.out.println("-----------------PROFILING--------------------");
        map.forEach((s, aLong) -> System.out.println(s + " - " + TimeUnit.MICROSECONDS.toMillis(aLong)));
        System.out.println("----------------------------------------------");
        System.out.println();
        map.clear();
    }
}
