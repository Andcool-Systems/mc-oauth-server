package com.andcool.handlers;

import com.andcool.MojangSession;
import com.andcool.OAuthServer;
import com.andcool.bytebuf.ByteBufUtils;
import com.andcool.config.UserConfig;
import com.andcool.encryption.Encryption;
import com.andcool.session.Session;
import com.andcool.session.SessionHandler;
import com.andcool.sillyLogger.Level;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class EncryptionHandler {

    /*
    Generate 6-digit code for client data
     */
    public static String getRandomCode() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(secureRandom.nextInt(10));
        }
        return code.toString();
    }

    public static void handleEncryptionResponse(ChannelHandlerContext ctx, ByteBuf in, Session session) {
        try {

            // Read encrypted shared secret and it's length
            int sharedSecretLength = ByteBufUtils.readVarInt(in);
            byte[] encryptedSharedSecret = new byte[sharedSecretLength];
            in.readBytes(encryptedSharedSecret);

            // Read encrypted verify token and it's length
            int verifyTokenLength = ByteBufUtils.readVarInt(in);
            byte[] encryptedVerifyToken = new byte[verifyTokenLength];
            in.readBytes(encryptedVerifyToken);

            // Init decrypt cipher for shared secret
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(Cipher.DECRYPT_MODE, OAuthServer.KEY_PAIR.getPrivate());

            // Trying to decrypt shared secret and verify token
            SecretKey sharedSecret = new SecretKeySpec(rsaCipher.doFinal(encryptedSharedSecret), "AES");
            byte[] verifyToken = rsaCipher.doFinal(encryptedVerifyToken);

            // Comparing our verify and decrypted verify tokens
            if (!Arrays.equals(OAuthServer.VERIFY_TOKEN, verifyToken)) {
                OAuthServer.logger.log(Level.ERROR, "Invalid verify token");
                SessionHandler.disconnect(ctx, "Error while encryption!");
                return;
            } else {
                OAuthServer.logger.log(Level.DEBUG, "Verify tokens match!");
            }

            // Creating hash for Mojang API
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(UserConfig.SERVER_ID.getBytes());
            digest.update(sharedSecret.getEncoded());
            digest.update(OAuthServer.KEY_PAIR.getPublic().getEncoded());
            String hash = new BigInteger(digest.digest()).toString(16);

            JSONObject response = MojangSession.sendRequest(session.nickname, hash);  // Do Mojang API request

            // Establishing encrypted connection with client
            ctx.pipeline().replace("encryption", "encryption", new Encryption(sharedSecret));

            // If client didn't Mojang API request (in offline mode)
            if (response == null) {
                SessionHandler.disconnect(ctx, "Failed to login: Invalid session (Try restarting your game and the launcher)");
                return;
            }

            // Create code for client
            // Disconnecting with code
            // Saving client data to hash map
            String code = getRandomCode();
            SessionHandler.disconnect(ctx, String.format("Hello, %s. Your code is: %s", session.nickname, code));
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("statusCode", 200);
            jsonResponse.put("nickname", response.getString("name"));
            jsonResponse.put("UUID", response.getString("id"));
            OAuthServer.expiringMap.put(code, jsonResponse);

            OAuthServer.logger.log(Level.INFO, "Created code " + code + " for " + session.nickname);
        } catch (IOException
                 | InterruptedException
                 | InvalidKeyException
                 | NoSuchAlgorithmException
                 | BadPaddingException
                 | IllegalBlockSizeException
                 | NoSuchPaddingException
                 | JSONException e) {
            OAuthServer.logger.log(Level.ERROR, e, true);
        }
    }
}
