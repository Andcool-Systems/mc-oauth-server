package com.andcool.responses;

import com.andcool.OAuthServer;
import com.andcool.bytebuf.ByteBufUtils;
import com.andcool.config.UserConfig;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;

import static com.andcool.bytebuf.ByteBufUtils.sendPacket;

public class PingResponse {
    public static String Response(int protoVersion) {
        JSONObject json_response = new JSONObject();

        JSONObject version = new JSONObject();
        JSONObject players = new JSONObject();
        JSONObject description = new JSONObject();

        version.put("name", UserConfig.SERVER_VERSION);
        version.put("protocol", protoVersion);
        json_response.put("version", version);

        players.put("max", UserConfig.PLAYERS_MAX);
        players.put("online", UserConfig.PLAYERS_NOW);
        players.put("sample", Collections.emptyList());
        json_response.put("players", players);

        description.put("text", "");
        description.put("extra", OAuthServer.MOTD_FORMATTER.format(UserConfig.MOTD));
        json_response.put("description", description);

        json_response.put("favicon", "data:image/png;base64," + OAuthServer.SERVER_ICON);
        json_response.put("enforcesSecureChat", false);
        json_response.put("previewsChat", false);

        return json_response.toString();
    }

    public static void pongResponse(ChannelHandlerContext ctx, long payload) {
        ByteBuf out = ctx.alloc().buffer();
        ByteBufUtils.writeVarInt(out, 0x01); // Packet ID
        out.writeLong(payload);
        sendPacket(ctx, out);
    }

    public static void sendPingResponse(ChannelHandlerContext ctx, int protoVersion) throws IOException {
        ByteBuf out = ctx.alloc().buffer();
        ByteBufUtils.writeVarInt(out, 0x00); // Packet ID
        ByteBufUtils.writeUTF8(out, PingResponse.Response(protoVersion));
        sendPacket(ctx, out);
    }
}
