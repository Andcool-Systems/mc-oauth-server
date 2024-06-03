package com.andcool.pipeline;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;

@Sharable
public final class NoopHandler extends ChannelHandlerAdapter {

    public static final NoopHandler INSTANCE = new NoopHandler();

    private NoopHandler() {
    }

}
