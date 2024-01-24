package cz.cvut.kbss.study.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.cvut.kbss.jopa.sessions.UnitOfWorkImpl;
import cz.cvut.kbss.study.rest.servlet.DiagnosticsContextFilter;
import cz.cvut.kbss.study.util.Constants;
import cz.cvut.kbss.study.util.json.ManageableIgnoreMixin;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;

@Configuration
public class WebAppConfig implements WebMvcConfigurer {

    @Bean(name = "objectMapper")
    @Primary
    public ObjectMapper objectMapper() {
        return createJsonObjectMapper();
    }

    /**
     * Creates an {@link ObjectMapper} for processing regular JSON.
     * <p>
     * This method is public static so that it can be used by the test environment as well.
     *
     * @return {@code ObjectMapper} instance
     */
    public static ObjectMapper createJsonObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // Ignore UoW references injected into entities
        objectMapper.addMixIn(UnitOfWorkImpl.class, ManageableIgnoreMixin.class);
        // JSR 310 (Java 8 DateTime API)
        objectMapper.registerModule(new JavaTimeModule());
        // Serialize datetime as ISO strings
        objectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        return objectMapper;
    }

    @Bean(name = "multipartResolver")
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public HttpMessageConverter<?> stringMessageConverter() {
        return new StringHttpMessageConverter(StandardCharsets.UTF_8);
    }

    @Bean
    public HttpMessageConverter<?> jsonMessageConverter(ObjectMapper objectMapper) {
        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        return converter;
    }

    @Bean
    public HttpMessageConverter<?> resourceMessageConverter() {
        return new ResourceHttpMessageConverter();
    }

    /**
     * Registers a filter that allows using current user's username in log pattern.
     */
    @Bean
    public FilterRegistrationBean<DiagnosticsContextFilter> mdcFilter() {
        FilterRegistrationBean<DiagnosticsContextFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new DiagnosticsContextFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer matcher) {
        matcher.addPathPrefix(Constants.REST_API_REFIX, HandlerTypePredicate.forAnnotation(RestController.class));
    }
}
