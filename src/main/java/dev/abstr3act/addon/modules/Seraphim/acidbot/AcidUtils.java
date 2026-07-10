package dev.abstr3act.addon.modules.Seraphim.acidbot;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class AcidUtils {
    public static JSONArray conversationHistory = new JSONArray();

    public static String getChatResponse(String userInput, String key, String api, String role) {
        String responseContent = "";

        try {
            URL url = new URL(api);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + key);
            connection.setDoOutput(true);
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("stream", false);
            if (conversationHistory.isEmpty()) {
                JSONObject systemMessage = new JSONObject();
                systemMessage.put("role", "system");
                systemMessage.put("content", role);
                conversationHistory.add(systemMessage);
            }

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", userInput);
            conversationHistory.add(userMessage);
            requestBody.put("messages", conversationHistory);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (InputStream is = connection.getInputStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
                StringBuilder response = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                JSONParser parser = new JSONParser();
                JSONObject responseJson = (JSONObject) parser.parse(response.toString());
                JSONArray choices = (JSONArray) responseJson.get("choices");
                JSONObject firstChoice = (JSONObject) choices.get(0);
                JSONObject message = (JSONObject) firstChoice.get("message");
                responseContent = (String) message.get("content");
                JSONObject assistantMessage = new JSONObject();
                assistantMessage.put("role", "assistant");
                assistantMessage.put("content", responseContent);
                conversationHistory.add(assistantMessage);
            }
        } catch (ParseException | IOException var23) {
            System.err.println("Error occurred while processing the request: " + var23.getMessage());
            var23.printStackTrace();
        }

        return responseContent;
    }
}
