package mamt.project.newapp.service;


import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class SalaireService {
    private final RestTemplate restTemplate = new RestTemplate();

    /*==========================================Detail de l employee(2-d)=====================================================*/

    public List<Map<String, Object>> getSalairesByEmployee(String sessionId, String employeeId) {
        String url = "http://erpnext.localhost:8000/api/resource/Salary Slip?" +
                "fields=[\"name\", \"employee\", \"employee_name\", \"gross_pay\", \"net_pay\", " +
                "\"start_date\", \"end_date\", \"posting_date\", \"salary_structure\"]" +
                "&filters=[[\"employee\",\"=\",\"" + employeeId + "\"]]" +
                "&order_by=start_date asc";

        return fetchData(sessionId, url);
    }
    /*==========================================Detail salaire(2-d)=====================================================*/
    public Map<String, Object> getSalaireDetails(String sessionId, String salaireId) {
        String url = "http://erpnext.localhost:8000/api/resource/Salary Slip/" + salaireId +
                "?fields=[\"name\", \"employee\", \"employee_name\", \"gross_pay\", \"net_pay\", " +
                "\"start_date\", \"end_date\", \"posting_date\", \"salary_structure\", " +
                "\"earnings.salary_component\", \"earnings.amount\", \"deductions.salary_component\", \"deductions.amount\"]";

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
            System.err.println("Error fetching salaire details: " + e.getMessage());
            return new HashMap<>();
        }
    }
    /*==========================================All salaire (2-e)(2-f)=====================================================*/
    public List<Map<String, Object>> getSalarySlips(String sessionId, Map<String, String> filters) {
        String baseUrl = "http://erpnext.localhost:8000/api/resource/Salary Slip?" +
                "fields=[\"name\", \"employee\", \"employee_name\", \"start_date\", \"end_date\",\"posting_date\",\"salary_structure\", \"net_pay\", \"total_deduction\"]";

        try {
            List<String> filterList = new ArrayList<>();

            String mois = filters.get("mois");
            String annee = filters.get("annee");
            String docstatus = filters.get("docstatus");

            filterList.add(String.format("[\"docstatus\", \"=\", \"%s\"]", docstatus));

            if (mois != null && !mois.isEmpty() && annee != null && !annee.isEmpty()) {
                int yearInt = Integer.parseInt(annee);
                int monthInt = Integer.parseInt(mois);
                String fromDate = annee + "-" + String.format("%02d", monthInt) + "-01";
                String toDate = annee + "-" + String.format("%02d", monthInt) + "-" + getLastDay(yearInt, monthInt);

                filterList.add(String.format("[\"start_date\", \">=\", \"%s\"]", fromDate));
                filterList.add(String.format("[\"end_date\", \"<=\", \"%s\"]", toDate));
            } else if (annee != null && !annee.isEmpty()) {
                String fromDate = annee + "-01-01";
                String toDate = annee + "-12-31";

                filterList.add(String.format("[\"start_date\", \">=\", \"%s\"]", fromDate));
                filterList.add(String.format("[\"end_date\", \"<=\", \"%s\"]", toDate));
            }
            baseUrl += "&limit=1000";

            String url = baseUrl;
            if (!filterList.isEmpty()) {
                url += "&filters=[" + String.join(",", filterList) + "]";
            }

            url += "&order_by=start_date asc";

            return fetchData(sessionId, url);
        } catch (Exception e) {
            System.err.println("Erreur getSalarySlips: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private int getLastDay(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return yearMonth.lengthOfMonth();
    }
    /*==========================================All salaire(2-e)=====================================================*/

    public void enrichirAvecComponents(String sessionId, List<Map<String, Object>> slips) {
        for (Map<String, Object> slip : slips) {
            String slipName = (String) slip.get("name");

            Map<String, Object> details = getSalaireDetails(sessionId, slipName);

            // Earnings
            List<Map<String, Object>> earnings = (List<Map<String, Object>>) details.getOrDefault("earnings", new ArrayList<>());
            double totalEarnings = earnings.stream()
                    .mapToDouble(e -> (double) e.getOrDefault("amount", 0)).sum();

            // Deductions
            List<Map<String, Object>> deductions = (List<Map<String, Object>>) details.getOrDefault("deductions", new ArrayList<>());
            double totalDeductions = deductions.stream()
                    .mapToDouble(d -> (double) d.getOrDefault("amount", 0)).sum();

            slip.put("earnings", earnings);
            slip.put("deductions", deductions);
            slip.put("total_earnings_components", totalEarnings);
            slip.put("total_deductions_components", totalDeductions);
        }
    }

    /*==========================================All salaire mensuel(f)=====================================================*/

    public List<Map<String, Object>> enrichirAvecComponentsMois(String sessionId, List<Map<String, Object>> slips) {
        Map<String, Map<String, Object>> groupedByMonth = new LinkedHashMap<>();

        for (Map<String, Object> slip : slips) {
            String slipName = (String) slip.get("name");
            Map<String, Object> details = getSalaireDetails(sessionId, slipName);
            String startDate = (String) details.get("start_date");
            String monthKey = startDate.substring(0, 7);

            // Tsy makao ra efa misy ilay key an groupedByMonth
            Map<String, Object> monthData = groupedByMonth.get(monthKey);//ex cles: "2024-06"
            if (monthData == null) {
                monthData = new HashMap<>();
                monthData.put("month", monthKey);
                monthData.put("total_net_pay", 0.0);
                monthData.put("employee_count", 0);
                monthData.put("earnings_components", new HashMap<String, Double>());
                monthData.put("deductions_components", new HashMap<String, Double>());
                groupedByMonth.put(monthKey, monthData);//mamorona eto
            }

            double netPay = details.get("net_pay") instanceof Number ? ((Number) details.get("net_pay")).doubleValue() : 0.0;
            monthData.put("total_net_pay", (Double) monthData.get("total_net_pay") + netPay);
            monthData.put("employee_count", (Integer) monthData.get("employee_count") + 1);

            Map<String, Double> earningsMap = (Map<String, Double>) monthData.get("earnings_components");
            List<Map<String, Object>> earnings = (List<Map<String, Object>>) details.getOrDefault("earnings", new ArrayList<>());

            for (Map<String, Object> earn : earnings) {
                String component = (String) earn.get("salary_component");
                double amount = earn.get("amount") instanceof Number ? ((Number) earn.get("amount")).doubleValue() : 0.0;

                if (earningsMap.containsKey(component)) {
                    earningsMap.put(component, earningsMap.get(component) + amount);
                } else {
                    earningsMap.put(component, amount);
                }
            }

            Map<String, Double> deductionsMap = (Map<String, Double>) monthData.get("deductions_components");
            List<Map<String, Object>> deductions = (List<Map<String, Object>>) details.getOrDefault("deductions", new ArrayList<>());

            for (Map<String, Object> deduct : deductions) {
                String component = (String) deduct.get("salary_component");
                double amount = deduct.get("amount") instanceof Number ? ((Number) deduct.get("amount")).doubleValue() : 0.0;

                if (deductionsMap.containsKey(component)) {
                    deductionsMap.put(component, deductionsMap.get(component) + amount);
                } else {
                    deductionsMap.put(component, amount);
                }
            }
        }

        // Calcul des totaux par mois
        for (Map<String, Object> monthData : groupedByMonth.values()) {
            Map<String, Double> earnings = (Map<String, Double>) monthData.get("earnings_components");
            Map<String, Double> deductions = (Map<String, Double>) monthData.get("deductions_components");

            double totalEarnings = 0.0;
            for (Double val : earnings.values()) totalEarnings += val;

            double totalDeductions = 0.0;
            for (Double val : deductions.values()) totalDeductions += val;

            monthData.put("total_earnings", totalEarnings);
            monthData.put("total_deductions", totalDeductions);
        }
        return new ArrayList<>(groupedByMonth.values());
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


}