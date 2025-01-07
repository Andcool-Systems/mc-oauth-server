package com.andcool.session;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public class SessionUtil {
    public static final AttributeKey<Session> SESSION_KEY = AttributeKey.valueOf("session");

    public static Session getSession(Channel channel) {
        return channel.attr(SESSION_KEY).get();
    }

    public static void setSession(Channel channel, Session session) {
        channel.attr(SESSION_KEY).set(session);
    }
}