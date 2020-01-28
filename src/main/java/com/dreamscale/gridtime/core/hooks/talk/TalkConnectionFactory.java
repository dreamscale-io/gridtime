package com.dreamscale.gridtime.core.hooks.talk;

import feign.auth.BasicAuthRequestInterceptor;
import org.dreamscale.feign.JacksonFeignBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Component
public class TalkConnectionFactory {

    @Autowired
    private JacksonFeignBuilder jacksonFeignBuilder;

    private TalkClient talkClient;

    @PostConstruct
    void initSharedTalkClient () {
        this.talkClient = jacksonFeignBuilder
                .requestInterceptor(new BasicAuthRequestInterceptor("admin", "p@ssw0rd123"))
                .target(TalkClient.class, "https://ds-talk.herokuapp.com");
    }

    public TalkConnection connect() {

        return new TalkConnection(talkClient);
    }


}
