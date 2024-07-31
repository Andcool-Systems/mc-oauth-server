package com.andcool.handlers;

import com.andcool.OAuthServer;
import com.andcool.session.Session;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import com.andcool.bytebuf.ByteBufUtils;
import com.andcool.sillyLogger.Level;

import static java.lang.String.format;
import com.andcool.pipeline.EncryptionRequest;

public class LoginStartHandler {
    public static void handleLoginStart(ChannelHandlerContext ctx, ByteBuf in, Session session) throws Exception {
        ByteBufUtils.readVarInt(in);  // unused
        ByteBufUtils.readVarInt(in);  // unused
        String nickname = ByteBufUtils.readUTF8(in);
        OAuthServer.logger.log(Level.DEBUG, format("Login start for: %s (protocol version %s)", nickname, session.protocolVersion));
        session.nickname = nickname;
        EncryptionRequest.sendEncryptionRequest(ctx, session.protocolVersion);
    }
}
