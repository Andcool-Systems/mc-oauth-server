package com.andcool;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import io.netty.handler.timeout.IdleStateHandler;
import org.json.JSONObject;

import com.andcool.config.UserConfig;
import com.andcool.format.MOTDFormatter;
import com.andcool.handlers.API.APIHandler;
import com.andcool.hashMap.ExpiringHashMap;
import com.andcool.pipeline.NoopHandler;
import com.andcool.session.SessionHandler;
import com.andcool.sillyLogger.Level;
import com.andcool.sillyLogger.SillyLogger;
import com.sun.net.httpserver.HttpServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;


public class OAuthServer {
    public static ExpiringHashMap<String, JSONObject> expiringMap = new ExpiringHashMap<>(5 * 60 * 1000);

    public static final KeyPair KEY_PAIR = generateKeyPair();
    public static final byte[] VERIFY_TOKEN = generateVerifyToken();
    public static final SillyLogger logger = new SillyLogger("Main Thread", true, Level.DEBUG);
    public static final MOTDFormatter MOTD_FORMATTER = new MOTDFormatter();
    public static final String SERVER_ICON = loadIcon();

    public static void main(String[] args) throws Exception {
        UserConfig.load();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new SessionHandler())
                                    .addLast("encryption", NoopHandler.INSTANCE)
                                    .addLast(new LengthFieldBasedFrameDecoder(8192, 0, 4, 0, 4))
                                    .addLast(new LengthFieldPrepender(4));
                        }
                    });

            HttpServer server = HttpServer.create(new InetSocketAddress(UserConfig.PORT_API), 0);
            server.createContext("/", new APIHandler());
            server.setExecutor(null);
            server.start();

            ChannelFuture future = b.bind(UserConfig.PORT_SERVER).sync();
            logger.log(Level.INFO, "Server started on port " + UserConfig.PORT_SERVER);
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] generateVerifyToken() {
        byte[] token = new byte[4];
        new SecureRandom().nextBytes(token);
        return token;
    }

    private static String loadIcon(){
        String imagePath = "server_icon.png";
        BufferedImage image;
        ByteArrayOutputStream outputStream;

        try {
            image = ImageIO.read(new File(imagePath));
            outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
        } catch (IOException e) {
            logger.log(Level.WARN, e.toString());
            return "";
        }
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }
}
