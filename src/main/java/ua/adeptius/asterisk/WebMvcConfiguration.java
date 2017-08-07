package ua.adeptius.asterisk;

//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.List;

//@Configuration
public class WebMvcConfiguration {
//
//
//    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//        MappingJackson2HttpMessageConverter jacksonMessageConverter = new MappingJackson2HttpMessageConverter();
//        ObjectMapper objectMapper = jacksonMessageConverter.getObjectMapper();
//        objectMapper.setVisibilityChecker(objectMapper.getDeserializationConfig().getDefaultVisibilityChecker()
//                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
//                .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
//                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
//                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
//                .withSetterVisibility(JsonAutoDetect.Visibility.NONE));
//
//        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
//
//        converters.add(jacksonMessageConverter);
//    }
}
