package com.andcool.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ByteBufUtils {

    public static String readUTF8(ByteBuf buf) throws IOException {
        int len = readVarInt(buf);
        byte[] bytes = new byte[len];
        buf.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void writeUTF8(ByteBuf buf, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length >= 32767) {
            throw new IOException("Attempt to write a string with a length greater than Short.MAX_VALUE to ByteBuf!");
        } else {
            writeVarInt(buf, bytes.length);
            buf.writeBytes(bytes);
        }
    }

    public static int readVarInt(ByteBuf buf) throws IOException {
        int out = 0;
        int bytes = 0;

        byte in;
        do {
            in = buf.readByte();
            out |= (in & 127) << bytes++ * 7;
            if (bytes > 5) {
                throw new IOException("Attempt to read int bigger than allowed for a varint!");
            }
        } while((in & 128) == 128);

        return out;
    }

    public static void writeVarInt(ByteBuf buf, int value) {
        do {
            byte part = (byte)(value & 127);
            value >>>= 7;
            if (value != 0) {
                part = (byte)(part | 128);
            }

            buf.writeByte(part);
        } while(value != 0);

    }

    public static void sendPacket(ChannelHandlerContext ctx, ByteBuf data) {
        ByteBuf packet = ctx.alloc().buffer();
        try {
            ByteBufUtils.writeVarInt(packet, data.readableBytes());
            packet.writeBytes(data);
            ctx.writeAndFlush(packet);
        } finally {
            data.release();
        }
    }
}
