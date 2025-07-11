package mamt.project.newapp.service.importation;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImportService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String BASE_URL = "http://erpnext.localhost:8000";
    private final String IMPORT_ENDPOINT = "/api/method/erpnext.api.import.import_fichier";

    public ResponseEntity<Map> importData(String sessionId,
                                          List<Map<String, Object>> employees,
                                          List<Map<String, Object>> salaryStructures,
                                          List<Map<String, Object>> salarySlips) {

        // Préparation des données à envoyer
        Map<String, List<Map<String, Object>>> requestBody = new HashMap<>();
        requestBody.put("employees", employees);
        requestBody.put("salaryStructures", salaryStructures);
        requestBody.put("salarySlips", salarySlips);

        try {
            // Configuration des headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Cookie", sessionId);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            // Création de la requête
            HttpEntity<Map<String, List<Map<String, Object>>>> request =
                    new HttpEntity<>(requestBody, headers);

            // Envoi de la requête POST
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + IMPORT_ENDPOINT,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            return response;

        } catch (Exception e) {
            System.err.println("Error during import: " + e.getMessage());
            // Retourner une réponse d'erreur vide
            return new ResponseEntity<>(new HashMap<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}