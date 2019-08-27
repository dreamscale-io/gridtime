package com.dreamscale.gridtime.core.hooks.realtime;

import feign.auth.BasicAuthRequestInterceptor;
import org.dreamscale.feign.JacksonFeignBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RealtimeConnectionFactory {

    @Autowired
    private JacksonFeignBuilder jacksonFeignBuilder;

    public RealtimeConnection connect() {

        RealtimeClient realtimeClient = jacksonFeignBuilder
                .requestInterceptor(new BasicAuthRequestInterceptor("flowserver", "replace_me_apikey"))
                .target(RealtimeClient.class, "https://ds-rt-flow.herokuapp.com");

        RealtimeConnection connection = new RealtimeConnection(realtimeClient);

        return connection;
    }


}
