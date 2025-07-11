package mamt.project.newapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ERPNextAuthService {

    private final String url_login = "http://erpnext.localhost:8000/api/method/login";
    private final Gson gson = new Gson();

    public String login(String username, String password) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> loginData = new HashMap<>();
        loginData.put("usr", username);
        loginData.put("pwd", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(loginData, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url_login,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            JsonObject responseJson = JsonParser.parseString(response.getBody()).getAsJsonObject();

            if (responseJson.has("message") && responseJson.get("message").getAsString().equals("Logged In")) {
                List<String> cookies = response.getHeaders().get("Set-Cookie");
                if (cookies != null) {
                    for (String cookie : cookies) {
                        if (cookie.startsWith("sid=")) {
                            return cookie;
                        }
                    }
                }
                return null;
            }
        }  catch (HttpClientErrorException.Unauthorized e) {
            return "Identifiants incorrects.";
        }
        return "cool";
    }
}
