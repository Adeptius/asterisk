package ua.adeptius.asterisk.spring_config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import ua.adeptius.asterisk.interceptors.AccessControlOriginInterceptor;

@Configuration
@ComponentScan("ua.adeptius")
public class WebConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new AccessControlOriginInterceptor()).addPathPatterns("/");
        registry.addInterceptor(new AccessControlOriginInterceptor());
    }

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public InternalResourceViewResolver resolver(){
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/");
        resolver.setSuffix(".html");
        System.out.println("             _            _   _           \n" +
                "    /\\      | |          | | (_)          \n" +
                "   /  \\   __| | ___ _ __ | |_ _ _   _ ___ \n" +
                "  / /\\ \\ / _` |/ _ \\ '_ \\| __| | | | / __|\n" +
                " / ____ \\ (_| |  __/ |_) | |_| | |_| \\__ \\\n" +
                "/_/    \\_\\__,_|\\___| .__/ \\__|_|\\__,_|___/\n" +
                "                   | |                    \n" +
                "                   |_|                    \n");
        return resolver;
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("/");
    }
}
