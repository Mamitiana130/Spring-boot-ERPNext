package mamt.project.newapp.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class EmployeService {
    private final RestTemplate restTemplate = new RestTemplate();
    /*==========================================All employees(c)=====================================================*/

    public List<Map<String, Object>> getEmployes(String sessionId, Map<String, String> filters) {
        String baseUrl = "http://erpnext.localhost:8000/api/resource/Employee?" +
                "fields=[\"name\", \"employee_name\", \"gender\", \"date_of_birth\",\"department\", \"designation\", \"status\"]";

        try {
            List<String> filterList = new ArrayList<>();
            filterList.add(String.format("[\"docstatus\", \"=\", \"1\"]"));

            if (filters != null) {
                if (filters.get("status") != null) {
                    filterList.add(String.format("[\"status\",\"=\",\"%s\"]", filters.get("status")));
                }
                if (filters.get("gender") != null) {
                    filterList.add(String.format("[\"gender\",\"=\",\"%s\"]", filters.get("gender")));
                }
                if (filters.get("birth_date_range") != null) {
                    String[] dates = filters.get("birth_date_range").split(",");
                    if (dates.length == 2) {
                        filterList.add(String.format("[\"date_of_birth\",\">=\",\"%s\"]", dates[0].trim()));
                        filterList.add(String.format("[\"date_of_birth\",\"<=\",\"%s\"]", dates[1].trim()));
                    }
                }
            }
            baseUrl += "&limit=1000";


            String url = baseUrl;
            if (!filterList.isEmpty()) {
                url += "&filters=[" + String.join(",", filterList) + "]";
            }

            return fetchData(sessionId, url);
        } catch (Exception e) {
            System.err.println("Error in getEmployes: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> fetchData(String sessionId, String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Cookie", sessionId);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("data")) {
                return (List<Map<String, Object>>) responseBody.get("data");
            }
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error fetching data from URL: " + url);
            System.err.println("Error details: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    /*==========================================Detail de l employee(d)=====================================================*/
    public Map<String, Object> getEmployeeDetails(String sessionId, String employeeId) {
        String url = "http://erpnext.localhost:8000/api/resource/Employee/" + employeeId +
                "?fields=[\"name\", \"employee_name\", \"gender\", \"date_of_birth\", " +
                "\"status\", \"company\", \"date_of_joining\",\"department\", \"designation\"]";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Cookie", sessionId);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("data")) {
                return (Map<String, Object>) responseBody.get("data");
            }
            return new HashMap<>();
        } catch (Exception e) {
            System.err.println("Error fetching employee details: " + e.getMessage());
            return new HashMap<>();
        }
    }



}