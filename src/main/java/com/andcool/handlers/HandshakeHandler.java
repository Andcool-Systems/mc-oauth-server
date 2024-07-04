package com.andcool.handlers;

import com.andcool.OAuthServer;
import com.andcool.bytebuf.ByteBufUtils;
import com.andcool.config.UserConfig;
import com.andcool.responses.PingResponse;
import com.andcool.session.Session;
import com.andcool.sillyLogger.Level;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class HandshakeHandler {
    public static void handleHandshake(ChannelHandlerContext ctx, ByteBuf in, Session session) throws Exception {
        int protocolVersion = ByteBufUtils.readVarInt(in);
        ByteBufUtils.readUTF8(in);
        in.readUnsignedShort();
        int nextState = ByteBufUtils.readVarInt(in);
        OAuthServer.logger.log(Level.DEBUG, "Received handshake! Protocol version: " +
                                                    protocolVersion +
                                                    " Next state: " + nextState);
        session.protocolVersion = protocolVersion;
        session.loginPhase = 1;
        session.nextState = nextState;
        switch (nextState){
            case 1 -> PingResponse.sendPingResponse(ctx, UserConfig.PROTOCOL_VERSION == -1 ? protocolVersion : UserConfig.PROTOCOL_VERSION);
            case 2 -> LoginStartHandler.handleLoginStart(ctx, in, session);
        }
    }
}
