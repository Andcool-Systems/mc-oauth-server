package com.andcool;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static java.lang.String.format;

import com.andcool.sillyLogger.Level;
import org.json.JSONObject;

public class MojangSession {
    public static JSONObject sendRequest(String username, String hash) throws IOException, InterruptedException {
        String BASE_URL = "https://sessionserver.mojang.com/session/minecraft/hasJoined";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(format("%s?username=%s&serverId=%s", BASE_URL, username, hash)))
                .build();

        OAuthServer.logger.log(Level.DEBUG, "Sending request to sessionserver.mojang.com");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        OAuthServer.logger.log(Level.DEBUG, "sessionserver.mojang.com answered with status code " + response.statusCode());
        if (response.statusCode() != 200) {
            return null;
        }
        return new JSONObject(response.body());
    }
}
