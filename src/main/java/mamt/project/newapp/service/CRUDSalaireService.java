package mamt.project.newapp.service;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class CRUDSalaireService {
    private final RestTemplate restTemplate = new RestTemplate();


    /*==========================================ancien alea 1=====================================================*/
    public List<Map<String, Object>> getSalaryStructuresAssignmentEmployeeFromDate(String sessionId,LocalDate date,String employee) {
        String url = "http://erpnext.localhost:8000/api/resource/Salary Structure Assignment?" +
                "fields=[\"name\",\"employee\", \"from_date\"]";
        try {
            List<String> filterList = new ArrayList<>();

            filterList.add(String.format("[\"employee\", \"=\", \"%s\"]", employee));
            filterList.add(String.format("[\"from_date\", \"=\", \"%s\"]", date));
            if (!filterList.isEmpty()) {
                url += "&filters=[" + String.join(",", filterList) + "]";
            }
            return fetchData(sessionId, url);
        } catch (Exception e) {
            System.err.println("Erreur getSalarySlips: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean fiscalYearExists(String sessionId, String year) {
        String url = "http://erpnext.localhost:8000/api/resource/Fiscal Year?" +
                "filters=[[\"name\",\"=\",\"" + year + "\"]]";
        List<Map<String, Object>> results = fetchData(sessionId, url);
        return !results.isEmpty();
    }

    public Map<String, Object> createFiscalYear(String sessionId, String year) {
        String url = "http://erpnext.localhost:8000/api/resource/Fiscal Year";

        Map<String, Object> data = new HashMap<>();
        data.put("name", year);
        data.put("year", year);
        data.put("year_start_date", year + "-01-01");
        data.put("year_end_date", year + "-12-31");

        return postData(sessionId, url, data);
    }

    public List<Map<String, Object>> getSalaryStructureAssignmentsWithFilters(String sessionId,Map<String, String> filters) {
        String url = "http://erpnext.localhost:8000/api/resource/Salary Structure Assignment?" +
                "fields=[\"name\",\"employee\", \"from_date\",\"base\",\"salary_structure\"]";
        try {
            List<String> filterList = new ArrayList<>();
            String employee = filters.get("employee");
            String fromDate = filters.get("from_date");
            if (employee!=null){
                filterList.add(String.format("[\"employee\", \"=\", \"%s\"]", employee));
            }
            if (fromDate!=null){
                filterList.add(String.format("[\"from_date\", \"<\", \"%s\"]", fromDate));
            }

            if (!filterList.isEmpty()) {
                url += "&filters=[" + String.join(",", filterList) + "]";
            }
            url += "&order_by=from_date asc";

            return fetchData(sessionId, url);
        } catch (Exception e) {
            System.err.println("Erreur getSalarySlips: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    public Map<String, Object> createSalaryStructureAssignment(String sessionId, String employee,String employeeName, String salaryStructure,
                                                               Double baseAmount, LocalDate startDate) {
        String url = "http://erpnext.localhost:8000/api/resource/Salary Structure Assignment";

        //jerena ra null na tsia le getSalaryStructuresAssignment
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("employee", employee);
        requestBody.put("employee_name", employeeName);
        requestBody.put("salary_structure", salaryStructure);
        requestBody.put("base", baseAmount);
        requestBody.put("from_date", startDate.toString());
        requestBody.put("docstatus", 1);

        return postData(sessionId, url, requestBody);
    }

    public Map<String, Object> createSalarySlip(String sessionId, String employee, String employeeName,
                                                String salaryStructure, LocalDate startDate, LocalDate endDate,
                                                LocalDate postingDate) {
        String url = "http://erpnext.localhost:8000/api/resource/Salary Slip";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", employee);
        requestBody.put("employee", employee);
        requestBody.put("employee_name", employeeName);
        requestBody.put("salary_structure", salaryStructure);
        requestBody.put("start_date", startDate.toString());
        requestBody.put("end_date", endDate.toString());
        requestBody.put("posting_date", postingDate.toString());
        requestBody.put("docstatus", 1);

        return postData(sessionId, url, requestBody);
    }

    private Map<String, Object> postData(String sessionId, String url, Map<String, Object> data) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Cookie", sessionId);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(data, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            return response.getBody();
        } catch (Exception e) {
            System.err.println("Error posting data to URL: " + url);
            System.err.println("Error details: " + e.getMessage());
            return Collections.singletonMap("error", e.getMessage());
        }
    }

    public List<Map<String, Object>> getSalaryStructures(String sessionId) {
        String url = "http://erpnext.localhost:8000/api/resource/Salary Structure?" +
                "fields=[\"name\", \"company\", \"payroll_frequency\", \"currency\"]" +
                "&limit=1000";

        return fetchData(sessionId, url);
    }
    public List<Map<String, Object>> getSalaryStructureAssignments(String sessionId) {
        String url = "http://erpnext.localhost:8000/api/resource/Salary Structure Assignment?" +
                "fields=[\"name\",\"employee_name\", \"from_date\", \"base\"]" +
                "&limit=1000";

        return fetchData(sessionId, url);
    }

    public List<Map<String, Object>> getEmployees(String sessionId) {
        String url = "http://erpnext.localhost:8000/api/resource/Employee?" +
                "fields=[\"name\", \"employee_name\", \"company\", \"department\"]" +
                "&limit=1000";

        return fetchData(sessionId, url);
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

    /*==========================================Update Salary Structure assignment=====================================================*/

    public Map<String, Object> getSalaryStructureAssignmentById(String sessionId, String assignmentId) {
        String url = "http://erpnext.localhost:8000/api/resource/Salary Structure Assignment/" + assignmentId;

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
            return Collections.singletonMap("error", "Données non trouvées");
        } catch (Exception e) {
            System.err.println("Error fetching salary structure assignment: " + e.getMessage());
            return Collections.singletonMap("error", e.getMessage());
        }
    }

    public Map<String, Object> updateSalaryAssignmentAndSlip(String sessionId, String assignId, String employee,
                                                             String employeeName, String salaryStructure,
                                                             LocalDate startDate, LocalDate endDate,
                                                             LocalDate postingDate, Double baseAmount) {
        // 1. Mise à jour de l'assignation de structure de salaire
        String assignmentUrl = "http://erpnext.localhost:8000/api/resource/Salary Structure Assignment/" + assignId;

        Map<String, Object> assignmentBody = new HashMap<>();
        assignmentBody.put("employee", employee);
        assignmentBody.put("employee_name", employeeName);
        assignmentBody.put("salary_structure", salaryStructure);
        assignmentBody.put("base", baseAmount);
        assignmentBody.put("from_date", startDate.toString());
        assignmentBody.put("docstatus", 0);

//        Map<String, Object> assignmentResult = putData(sessionId, assignmentUrl, assignmentBody);
        return putData(sessionId, assignmentUrl, assignmentBody);

//        if (assignmentResult.containsKey("error")) {
//            return assignmentResult;
//        }
//
//        // 2. Mise à jour du bulletin de salaire correspondant
//        // On suppose que le nom du bulletin de salaire est le même que l'ID employé
//        String slipUrl = "http://erpnext.localhost:8000/api/resource/Salary Slip/" + employee;
//
//        Map<String, Object> slipBody = new HashMap<>();
//        slipBody.put("employee", employee);
//        slipBody.put("employee_name", employeeName);
//        slipBody.put("salary_structure", salaryStructure);
//        slipBody.put("start_date", startDate.toString());
//        slipBody.put("end_date", endDate.toString());
//        slipBody.put("posting_date", postingDate.toString());
//        slipBody.put("docstatus", 1);
//
//        return putData(sessionId, slipUrl, slipBody);
    }

    private Map<String, Object> putData(String sessionId, String url, Map<String, Object> data) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Cookie", sessionId);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(data, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    request,
                    Map.class
            );

            return response.getBody();
        } catch (Exception e) {
            System.err.println("Error updating data at URL: " + url);
            System.err.println("Error details: " + e.getMessage());
            return Collections.singletonMap("error", e.getMessage());
        }
    }

//    private Map<String, Object> fetchSingleData(String sessionId, String url) {
//        try {
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Cookie", sessionId);
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            HttpEntity<String> request = new HttpEntity<>(headers);
//            ResponseEntity<Map> response = restTemplate.exchange(
//                    url,
//                    HttpMethod.GET,
//                    request,
//                    Map.class
//            );
//
//            Map<String, Object> responseBody = response.getBody();
//            if (responseBody != null && responseBody.containsKey("data")) {
//                return (Map<String, Object>) responseBody.get("data");
//            }
//
//            System.out.println("tsisy");
//            return null;
//
//        } catch (Exception e) {
//            System.err.println("Erreur lors de la récupération du document: " + e.getMessage());
//            return null;
//        }
//    }


}
