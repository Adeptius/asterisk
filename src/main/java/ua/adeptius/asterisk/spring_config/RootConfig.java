package ua.adeptius.asterisk.spring_config;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class RootConfig {

//    @Bean
//    public PropertyPlaceholderConfigurer getPropertyPlaceholderConfigurer() {
//        PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
//        ppc.setLocation(new ClassPathResource("localdb.properties"));
//        ppc.setIgnoreUnresolvablePlaceholders(true);
//        return ppc;
//    }
}
