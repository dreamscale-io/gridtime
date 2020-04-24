package com.dreamscale.gridtime.core.hooks.talk;

import feign.auth.BasicAuthRequestInterceptor;
import org.dreamscale.feign.JacksonFeignBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class TalkClientConnectionFactory {

    @Autowired
    private JacksonFeignBuilder jacksonFeignBuilder;

    private TalkClient talkClient;

    @Value( "${talk.connect.url}" )
    private String talkUrl;

    @Value( "${talk.connect.username}" )
    private String talkUserName;

    @Value( "${talk.connect.password}" )
    private String talkPassword;

    @PostConstruct
    void initSharedTalkClient () {
        this.talkClient = jacksonFeignBuilder
                .requestInterceptor(new BasicAuthRequestInterceptor(talkUserName, talkPassword))
                .target(TalkClient.class, talkUrl);
    }

    public TalkClientConnection connect() {

        return new TalkClientConnection(talkClient);
    }


}
