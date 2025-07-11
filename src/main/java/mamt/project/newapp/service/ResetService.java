package mamt.project.newapp.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ResetService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String BASE_URL = "http://erpnext.localhost:8000";
//    /api/method/erpnext.api.import.import_fichier
    private final String RESET_ENDPOINT = "/api/method/erpnext.reset.reset_data.vider_tables";

    public void reset(String sessionId) {
        try {
            // Configuration des headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Cookie", sessionId);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            // Création de la requête (sans body car c'est probablement une requête GET)
            HttpEntity<String> request = new HttpEntity<>(headers);

            // Envoi de la requête GET
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + RESET_ENDPOINT,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            // Vous pourriez traiter la réponse ici si nécessaire
            if (!response.getStatusCode().is2xxSuccessful()) {
                System.err.println("Erreur lors de la réinitialisation: " + response.getBody());
            }

        } catch (Exception e) {
            System.err.println("Error during reset: " + e.getMessage());
            throw new RuntimeException("Échec de la réinitialisation", e);
        }
    }
}