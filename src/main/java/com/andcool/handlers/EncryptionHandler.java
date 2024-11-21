package com.andcool.handlers;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONObject;

import com.andcool.MojangSession;
import com.andcool.OAuthServer;
import com.andcool.bytebuf.ByteBufUtils;
import com.andcool.config.UserConfig;
import com.andcool.session.Session;
import com.andcool.session.SessionHandler;
import com.andcool.sillyLogger.Level;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class EncryptionHandler {
    public static void handleEncryptionResponse(ChannelHandlerContext ctx, ByteBuf in, Session session) throws Exception {
        try {
            int sharedSecretLength = ByteBufUtils.readVarInt(in);
            byte[] encryptedSharedSecret = new byte[sharedSecretLength];
            in.readBytes(encryptedSharedSecret);

            int verifyTokenLength = ByteBufUtils.readVarInt(in);
            byte[] encryptedVerifyToken = new byte[verifyTokenLength];
            in.readBytes(encryptedVerifyToken);

            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(Cipher.DECRYPT_MODE, OAuthServer.KEY_PAIR.getPrivate());
            SecretKey sharedSecret;

            sharedSecret = new SecretKeySpec(rsaCipher.doFinal(encryptedSharedSecret), "AES");
            byte[] verifyToken = rsaCipher.doFinal(encryptedVerifyToken);

            if (!Arrays.equals(OAuthServer.VERIFY_TOKEN, verifyToken)) {
                OAuthServer.logger.log(Level.DEBUG, "Invalid verify token");
                SessionHandler.disconnect(ctx, "Error while encryption!");
                return;
            } else {
                OAuthServer.logger.log(Level.DEBUG, "Verify tokens match!");
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(UserConfig.SERVER_ID.getBytes());
            digest.update(sharedSecret.getEncoded());
            digest.update(OAuthServer.KEY_PAIR.getPublic().getEncoded());

            String hash = new BigInteger(digest.digest()).toString(16);
            JSONObject response = MojangSession.sendRequest(session.nickname, hash);
            ctx.pipeline().replace("encryption", "encryption", new Encryption(sharedSecret));

            if (response == null) {
                SessionHandler.disconnect(ctx, "§cYou are using unlicensed copy of Minecraft!");
                return;
            }

            Random random = new Random();
            int code = 100000 + random.nextInt(900000);

            SessionHandler.disconnect(ctx, "§l§aYour code is: §n" + code + "§r");
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("status", "success");
            jsonResponse.put("nickname", response.getString("name"));
            jsonResponse.put("UUID", response.getString("id"));
            OAuthServer.expiringMap.put(String.valueOf(code), jsonResponse);

            OAuthServer.logger.log(Level.INFO, "Created code " + code + " for " + session.nickname);
        } catch (IOException | InterruptedException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | JSONException e) {
            OAuthServer.logger.log(Level.DEBUG, "Exception in handleEncryptionResponse: " + e.toString());
        } finally {
            //in.release(); // Освобождение буфера
        }
    }
}
