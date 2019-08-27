package com.dreamscale.gridtime;

import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.security.WebSecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.feign.DefaultFeignConfig;
import org.dreamscale.springboot.config.CommonSpringBootConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

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

    @Bean
    public RequestContext requestContext() {
        return new RequestContext();
    }

}
