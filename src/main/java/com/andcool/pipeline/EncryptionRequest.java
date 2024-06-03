package com.andcool.pipeline;

import com.andcool.OAuthServer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import com.andcool.bytebuf.ByteBufUtils;

import java.io.IOException;

public class EncryptionRequest {
    public static void sendEncryptionRequest(ChannelHandlerContext ctx) throws IOException {
        ByteBuf out = ctx.alloc().buffer();
        ByteBufUtils.writeVarInt(out, 0x01); // Packet ID
        ByteBufUtils.writeUTF8(out, OAuthServer.server_id); // Server ID
        byte[] publicKey = OAuthServer.KEY_PAIR.getPublic().getEncoded();

        ByteBufUtils.writeVarInt(out, publicKey.length);
        out.writeBytes(publicKey);

        ByteBufUtils.writeVarInt(out, OAuthServer.VERIFY_TOKEN.length);
        out.writeBytes(OAuthServer.VERIFY_TOKEN);

        ByteBufUtils.sendPacket(ctx, out);
    }
}
