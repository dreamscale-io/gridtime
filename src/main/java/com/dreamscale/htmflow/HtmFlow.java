package com.dreamscale.htmflow;

import org.dreamscale.feign.DefaultFeignConfig;
import org.dreamscale.feign.JacksonFeignBuilder;
import org.dreamscale.springboot.config.CommonSpringBootConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ComponentScan("com.dreamscale.htmflow")
@Import({
        DefaultFeignConfig.class,
        CommonSpringBootConfig.class
})
public class HtmFlow {

    public static void main(String[] args) {
        SpringApplication.run(HtmFlow.class, args);
    }

}
