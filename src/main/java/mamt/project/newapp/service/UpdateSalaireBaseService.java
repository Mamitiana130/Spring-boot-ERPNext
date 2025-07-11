package mamt.project.newapp.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class UpdateSalaireBaseService {
    @Autowired
    private SalaireService salaireService ;
    private final RestTemplate restTemplate = new RestTemplate();
    public List<Map<String, Object>> getSalaryComponents(String sessionId) {
        String url = "http://erpnext.localhost:8000/api/resource/Salary Component?" +
                "fields=[\"name\",\"type\"]" +
                "&limit=1000";

        return fetchData(sessionId, url);
    }


    /*===================================================Update salary Structure Assignment/Slip(ancien alea 2)=================================================*/
    public List<Map<String, Object>> getSalarySlipsWithCondition(String sessionId,
                                                                 String componentName,
                                                                 Double montant,
                                                                 String condition) {
        List<Map<String, Object>> filteredSlips = new ArrayList<>();

        Map<String, String> filters = new HashMap<>();
        filters.put("docstatus","1");

        List<Map<String, Object>> allSlips = salaireService.getSalarySlips(sessionId, filters);

        for (Map<String, Object> slip : allSlips) {
            String slipName = (String) slip.get("name");
            Map<String, Object> slipDetails = salaireService.getSalaireDetails(sessionId, slipName);

            List<Map<String, Object>> earnings = (List<Map<String, Object>>) slipDetails.get("earnings");
            if (earnings != null) {
                for (Map<String, Object> earning : earnings) {
                    if (matchesCondition(earning, componentName, montant, condition)) {
                        filteredSlips.add(slip);
                        break; // tsy mila jerena tsony ny ambony reefa azo ny ray
                    }
                }
            }
            List<Map<String, Object>> deductions = (List<Map<String, Object>>) slipDetails.get("deductions");
            if (deductions != null) {
                for (Map<String, Object> deduction : deductions) {
                    if (matchesCondition(deduction, componentName, montant, condition)) {
                        filteredSlips.add(slip);
                        break;
                    }
                }
            }
        }

        return filteredSlips;
    }

    private boolean matchesCondition(Map<String, Object> component,
                                     String componentName,
                                     Double montant,
                                     String condition) {
        String currentComponentName = (String) component.get("salary_component");
        Double currentAmount = Double.parseDouble(component.get("amount").toString());

        if (!currentComponentName.equals(componentName)) {
            return false;
        }

        if (condition == null || condition.isEmpty()) {
            return true;
        }

        switch (condition) {
            case "=":
                return currentAmount.equals(montant);
            case "<":
                return currentAmount < montant;
            case ">":
                return currentAmount > montant;
            case "<=":
                return currentAmount <= montant;
            case ">=":
                return currentAmount >= montant;
            default:
                return true;
        }
    }
    public Map<String, Object> cancelDocument(String sessionId, String doctype, String name) {
        String url = "http://erpnext.localhost:8000/api/method/frappe.client.cancel";

        Map<String, Object> body = new HashMap<>();
        body.put("doctype", doctype);
        body.put("name", name);

        return postData(sessionId, url, body);
    }
    public Map<String, Object> cancelDocumentByEmployeeStartDate(String sessionId, String doctype, String employee,String startDate) {
        String url = "http://erpnext.localhost:8000/api/method/frappe.client.cancel";

        Map<String, Object> body = new HashMap<>();
        body.put("doctype", doctype);
        body.put("employee", employee);
        body.put("start_date", startDate);

        return postData(sessionId, url, body);
    }
    public List<Map<String, Object>> getSalaryAssignmentsForEmployee(String sessionId,String employee,String startDate) {
        String url = "http://erpnext.localhost:8000/api/resource/Salary Structure Assignment?" +
                "fields=[\"name\",\"employee\",\"employee_name\",\"salary_structure\", \"base\", \"from_date\"]";
        try {
            List<String> filterList = new ArrayList<>();

            filterList.add(String.format("[\"docstatus\", \"=\", \"1\"]"));
            filterList.add(String.format("[\"employee\", \"=\", \"%s\"]", employee));
            filterList.add(String.format("[\"from_date\", \"=\", \"%s\"]", startDate));

            if (!filterList.isEmpty()) {
                url += "&filters=[" + String.join(",", filterList) + "]";
            }
            return fetchData(sessionId, url);
        } catch (Exception e) {
            System.err.println("Erreur getSalarySlips: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    public List<Map<String, Object>> getSalaryAssignments(String sessionId) {
        String url = "http://erpnext.localhost:8000/api/resource/Salary Structure Assignment?" +
                "fields=[\"name\",\"employee\",\"employee_name\",\"salary_structure\", \"base\", \"from_date\"]";
        try {
            List<String> filterList = new ArrayList<>();

            filterList.add(String.format("[\"docstatus\", \"=\", \"1\"]"));

            if (!filterList.isEmpty()) {
                url += "&filters=[" + String.join(",", filterList) + "]";
            }
            return fetchData(sessionId, url);
        } catch (Exception e) {
            System.err.println("Erreur getSalarySlips: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public int getLastDay(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return yearMonth.lengthOfMonth();
    }
    /*=========================================================Utilities=================================================================================*/

    public Map<String, Object> submitDocument(String sessionId, String doctype, String name) {
        String url = "http://erpnext.localhost:8000/api/method/frappe.client.submit";

        Map<String, Object> body = new HashMap<>();
        body.put("doctype", doctype);
        body.put("name", name);

        return postData(sessionId, url, body);
    }

    public Map<String, Object> setDocumentToDraft(String sessionId, String doctype, String name) {
        String url = "http://erpnext.localhost:8000/api/method/frappe.client.set_value";

        Map<String, Object> body = new HashMap<>();
        body.put("doctype", doctype);
        body.put("name", name);
        body.put("fieldname", "docstatus");
        body.put("value", 0);  // 0 = Draft, 1 = Submitted, 2 = Cancelled

        return postData(sessionId, url, body);
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
    private Map<String, Object> fetchSingleData(String sessionId, String url) {
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
            throw new RuntimeException("Document non trouvé");
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération du document", e);
        }
    }

}
