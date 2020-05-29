package com.dreamscale.gridtime;

import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.security.WebSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.feign.DefaultFeignConfig;
import org.dreamscale.springboot.config.CommonSpringBootConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@SpringBootApplication
@ComponentScan("com.dreamscale.gridtime")
@Import({
        DefaultFeignConfig.class,
        CommonSpringBootConfig.class,
        WebSecurityConfig.class
})

@Slf4j
public class GridTime {

    public static void main(String[] args) {

        SpringApplication.run(GridTime.class, args);
    }

    @Autowired
    ObjectMapper objectMapper;

    /**
     * Even though the ObjectMapper is defined as @Primary in CommonSpringConfig, it is not getting used to convert
     * the request/responses.  There is a potential explanation of why here https://springfox.github.io/springfox/docs/current/,
     * under the section "Object Mapper Customizations Not Working?".  While the provided solution in those docs does
     * not seem work, specifically injecting the ObjectMapper by name and setting it directly on the
     * MappingJackson2HttpMessageConverter does.
     */
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
                for (HttpMessageConverter converter : converters) {
                    if (converter instanceof MappingJackson2HttpMessageConverter) {
                        MappingJackson2HttpMessageConverter jacksonConverter =
                                ((MappingJackson2HttpMessageConverter) converter);
                        if (jacksonConverter.getObjectMapper() != null) {
                            jacksonConverter.setObjectMapper(objectMapper);
                        }
                    }
                }
            }
        };
    }

    @Bean
    public RequestContext requestContext() {
        return new RequestContext();
    }

}
