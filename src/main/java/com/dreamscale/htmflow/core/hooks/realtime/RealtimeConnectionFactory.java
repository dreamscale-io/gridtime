package com.dreamscale.htmflow.core.hooks.realtime;

import com.dreamscale.htmflow.core.hooks.jira.JiraClient;
import com.dreamscale.htmflow.core.hooks.jira.JiraConnection;
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
