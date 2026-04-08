package com.chatapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Service
public class AIService {

    @Value("${groq.api.key:disabled}")
    private String apiKey;

    public List<String> getSmartReplies(String message) {
        if (apiKey.equals("disabled") || apiKey.isBlank()) {
            return Arrays.asList("Okay!");
        }
        try {
            String reply = callGroq(message);
            return Arrays.asList(reply);
        } catch (Exception e) {
            System.out.println("=== Groq error: " + e.getMessage());
            return Arrays.asList("Okay!");
        }
    }

    public String getSmartReply(String message) {
        return getSmartReplies(message).get(0);
    }

    private String callGroq(String userMessage) throws Exception {
        URL url = new URL("https://api.groq.com/openai/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);

        String safeMessage = userMessage
                .replace("\\", "\\\\")
                .replace("\"", "'")
                .replace("\n", " ")
                .replace("\r", " ");

        String body = "{"
            + "\"model\": \"llama-3.3-70b-versatile\","
            + "\"messages\": ["
            + "  {\"role\": \"system\", \"content\": \"You are a chat assistant. Reply with ONE short natural conversational response under 10 words. No explanations. Just the reply.\"},"
            + "  {\"role\": \"user\", \"content\": \"" + safeMessage + "\"}"
            + "],"
            + "\"max_tokens\": 30,"
            + "\"temperature\": 0.7"
            + "}";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes("UTF-8"));
        }

        int responseCode = conn.getResponseCode();

        BufferedReader reader;
        if (responseCode == 200) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        System.out.println("=== Groq response code: " + responseCode);
        System.out.println("=== Groq raw response: " + response.toString());

        if (responseCode != 200) {
            throw new Exception("Groq API error " + responseCode + ": " + response.toString());
        }

        // Parse content from JSON
        String json = response.toString();
        int start = json.indexOf("\"content\":\"") + 11;
        int end = json.indexOf("\"", start);

        if (start < 11 || end < 0) {
            throw new Exception("Could not parse response: " + json);
        }

        return json.substring(start, end).trim();
    }
}