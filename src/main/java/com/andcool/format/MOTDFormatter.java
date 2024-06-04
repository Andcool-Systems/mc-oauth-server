package com.andcool.format;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MOTDFormatter {
    private static final char COLOR_CHAR = '&';
    private final Map<Character, String> colorMap = new HashMap<>();
    private final Map<Character, String> styleMap = new HashMap<>();

    public MOTDFormatter() {
        colorMap.put('0', "black");
        colorMap.put('1', "dark_blue");
        colorMap.put('2', "dark_green");
        colorMap.put('3', "dark_aqua");
        colorMap.put('4', "dark_red");
        colorMap.put('5', "dark_purple");
        colorMap.put('6', "gold");
        colorMap.put('7', "gray");
        colorMap.put('8', "dark_gray");
        colorMap.put('9', "blue");
        colorMap.put('a', "green");
        colorMap.put('b', "aqua");
        colorMap.put('c', "red");
        colorMap.put('d', "light_purple");
        colorMap.put('e', "yellow");
        colorMap.put('f', "white");

        styleMap.put('k', "obfuscated");
        styleMap.put('l', "bold");
        styleMap.put('m', "strikethrough");
        styleMap.put('n', "underlined");
        styleMap.put('o', "italic");
        styleMap.put('r', "reset");
    }

    public JSONArray format(String motd) {
        motd = motd.replace("§", "&");  // Because JAVA not working with '§' symbol. idk
        JSONArray jsonArray = new JSONArray();
        StringBuilder currentText = new StringBuilder();
        JSONObject currentJson = new JSONObject();

        for (int i = 0; i < motd.length(); i++) {
            char c = motd.charAt(i);
            if (c == COLOR_CHAR && i + 1 < motd.length()) {
                if (!currentText.isEmpty()) {
                    currentJson.put("text", currentText.toString());
                    jsonArray.put(currentJson);
                    currentText = new StringBuilder();
                    currentJson = new JSONObject();
                }

                char formatChar = motd.charAt(i + 1);
                if (colorMap.containsKey(formatChar)) {
                    currentJson.put("color", colorMap.get(formatChar));
                } else if (styleMap.containsKey(formatChar)) {
                    currentJson.put(styleMap.get(formatChar), true);
                }

                i++;
            } else {
                currentText.append(c);
            }
        }

        if (!currentText.isEmpty()) {
            currentJson.put("text", currentText.toString());
            jsonArray.put(currentJson);
        }
        return jsonArray;
    }
}
