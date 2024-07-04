package com.andcool.handlers.API;

import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.andcool.OAuthServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class APIHandler implements HttpHandler {
    private final Pattern pattern = Pattern.compile("/code/(\\w+)");

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        Matcher matcher = pattern.matcher(path);

        int status_code = 200;
        String response;

        if (matcher.matches()) {
            String code = matcher.group(1);
            JSONObject result = OAuthServer.expiringMap.get(code);
            if (result == null){
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Code not found");
                jsonResponse.put("status_code", 404);
                response = jsonResponse.toString();
                status_code = 404;
            }else{
                response = result.toString();
                OAuthServer.expiringMap.remove(code);
            }
        } else {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Not found");
            jsonResponse.put("status_code", 404);
            response = jsonResponse.toString();
            status_code = 404;
        }

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status_code, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
