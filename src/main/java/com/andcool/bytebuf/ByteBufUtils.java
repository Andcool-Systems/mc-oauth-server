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

    public static long readVarLong(ByteBuf in) {
        long value = 0;
        int position = 0;
        byte currentByte;

        while (true) {
            currentByte = in.readByte();
            value |= (long) (currentByte & 0x7F) << position;

            if ((currentByte & 0x80) == 0) break;

            position += 7;

            if (position >= 64) throw new RuntimeException("VarLong is too big");
        }

        return value;
    }

    public static void writeVarLong(ByteBuf buf, long value) {
        do {
            byte part = (byte)((int)(value & 127L));
            value >>>= 7;
            if (value != 0L) {
                part = (byte)(part | 128);
            }

            buf.writeByte(part);
        } while(value != 0L);

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
