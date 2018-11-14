package com.dreamscale.htmflow;

import com.dreamscale.htmflow.core.security.RequestContext;
import com.dreamscale.htmflow.core.security.WebSecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.feign.DefaultFeignConfig;
import org.dreamscale.springboot.config.CommonSpringBootConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ComponentScan("com.dreamscale.htmflow")
@Import({
        DefaultFeignConfig.class,
        CommonSpringBootConfig.class,
        WebSecurityConfig.class
})

@Slf4j
public class HtmFlow {


    public static void main(String[] args) {

        SpringApplication.run(HtmFlow.class, args);
    }

    @Bean
    public RequestContext requestContext() {
        return new RequestContext();
    }

}
