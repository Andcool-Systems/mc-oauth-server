package com.andcool.session;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import com.andcool.OAuthServer;
import com.andcool.bytebuf.ByteBufUtils;
import com.andcool.handlers.EncryptionHandler;
import com.andcool.handlers.HandshakeHandler;
import com.andcool.responses.PingResponse;
import com.andcool.sillyLogger.Level;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.concurrent.Executors;

public class SessionHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Session session = new Session();
        SessionUtil.setSession(ctx.channel(), session);
        ctx.fireChannelActive();

        scheduler.schedule(() -> {
            ctx.close();
        }, 15, TimeUnit.SECONDS);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        try {
            Session session = SessionUtil.getSession(ctx.channel());
            int packetLength = ByteBufUtils.readVarInt(in);
            int packetId = ByteBufUtils.readVarInt(in);
            OAuthServer.logger.log(Level.DEBUG, "packet id: " + packetId + " packet length: " + packetLength);

            switch (packetId) {
                case 0x00 -> // Handshake
                    HandshakeHandler.handleHandshake(ctx, in, session);
                case 0x01 -> {
                    if (session.nextState == 1) { // Ping/Pong request
                        long payload = in.readLong();
                        PingResponse.pongResponse(ctx, payload);
                    }
                    if (session.nextState == 2) {  // Encryption response
                        EncryptionHandler.handleEncryptionResponse(ctx, in, session);
                    }
                }
                default -> OAuthServer.logger.log(Level.ERROR, "Invalid packet ID: " + packetId);
            }
        }catch (Exception e){
            OAuthServer.logger.log(Level.ERROR, e.toString());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws IOException {
        disconnect(ctx, "§cInternal server exception");
        OAuthServer.logger.log(Level.ERROR, cause.toString());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {  // Client disconnect
        OAuthServer.logger.log(Level.DEBUG, "Session closed!");
        ctx.fireChannelInactive();
        scheduler.shutdown();
    }

    public static void disconnect(ChannelHandlerContext ctx, String reason) throws IOException {
        ByteBuf out = ctx.alloc().buffer();
        ByteBufUtils.writeVarInt(out, 0x00); // Packet ID
        JSONObject response = new JSONObject();
        response.put("text", "");
        response.put("extra", OAuthServer.MOTD_FORMATTER.format(reason));
        ByteBufUtils.writeUTF8(out, response.toString());
        ctx.channel().writeAndFlush(out).addListener(ChannelFutureListener.CLOSE);
    }
}