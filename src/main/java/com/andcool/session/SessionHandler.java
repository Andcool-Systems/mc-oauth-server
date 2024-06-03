package com.andcool.session;

import com.andcool.OAuthServer;
import com.andcool.handlers.HandshakeHandler;
import com.andcool.sillyLogger.Level;
import com.andcool.bytebuf.ByteBufUtils;
import com.andcool.handlers.EncryptionHandler;
import com.andcool.responses.PingResponse;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.json.JSONObject;

import java.io.IOException;

public class SessionHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Session session = new Session();
        SessionUtil.setSession(ctx.channel(), session);
        ctx.fireChannelActive();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Session session = SessionUtil.getSession(ctx.channel());
        int packetLength = ByteBufUtils.readVarInt(in);
        int packetId = ByteBufUtils.readVarInt(in);
        OAuthServer.logger.log(Level.DEBUG, "packet id: " + packetId + " packet length: " + packetLength);

        switch (packetId) {
            case 0x00: // Handshake
                HandshakeHandler.handleHandshake(ctx, in, session);
                break;
            case 0x01:
                if (session.nextState == 1) { // Ping/Pong request
                    long payload = in.readLong();
                    PingResponse.pongResponse(ctx, payload);
                }
                if (session.nextState == 2) {  // Encryption response
                    EncryptionHandler.handleEncryptionResponse(ctx, in, session);
                }
                break;
            default:
                OAuthServer.logger.log(Level.ERROR, "Invalid packet ID: " + packetId);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws IOException {
        disconnect(ctx, "Internal server exception");
        OAuthServer.logger.log(Level.ERROR, cause.toString());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {  // Client disconnect
        ctx.fireChannelInactive();
    }

    public static void disconnect(ChannelHandlerContext ctx, String reason) throws IOException {
        ByteBuf out = ctx.alloc().buffer();
        ByteBufUtils.writeVarInt(out, 0x00); // Packet ID
        JSONObject response = new JSONObject();
        response.put("text", reason);
        ByteBufUtils.writeUTF8(out, response.toString());
        ctx.channel().writeAndFlush(out).addListener(ChannelFutureListener.CLOSE);
    }
}