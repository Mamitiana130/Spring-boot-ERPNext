package mamt.project.newapp.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class ExoService {
    @Autowired
    private SalaireService salaireService ;
    private final RestTemplate restTemplate = new RestTemplate();
    public List<Map<String, Object>> getSalarySlipsEmployeesAverages(List<Map<String, Object>> slips,
                                                                     List<Map<String, Object>> employees) {
        List<Map<String, Object>> result = new ArrayList<>();

        Map<String, Map<String, Object>> employeesByName = new HashMap<>();
        for (Map<String, Object> employee : employees) {
            String employeeName = (String) employee.get("employee_name");
            employeesByName.put(employeeName, employee);
        }

        Map<String, Double> totalsByGender = new HashMap<>();
        Map<String, Integer> countsByGender = new HashMap<>();

        for (Map<String, Object> slip : slips) {
            String employeeName = (String) slip.get("employee_name");
            Map<String, Object> employee = employeesByName.get(employeeName);

            if (employee != null) {
                String gender = (String) employee.get("gender");
                double netPay = slip.get("net_pay") instanceof Number ?
                        ((Number) slip.get("net_pay")).doubleValue() : 0.0;

                totalsByGender.merge(gender, netPay, Double::sum);
                countsByGender.merge(gender, 1, Integer::sum);
            }
        }

        //foreach(K,V)
        totalsByGender.forEach((gender, total) -> {
            int count = countsByGender.getOrDefault(gender, 1);
            double average = total / count;

            Map<String, Object> entry = new HashMap<>();
            entry.put("sexe", gender);
            entry.put("amount", average);
            result.add(entry);
        });

        return result;
    }

    public List<Map<String, Object>> getAllAverage(List<Map<String, Object>> structures) {
        List<Map<String, Object>> result = new ArrayList<>();



        Map<String, Double> totals = new HashMap<>();
        Map<String, Integer> counts = new HashMap<>();

        for (Map<String, Object> structure : structures) {

                double base = structure.get("base") instanceof Number ?
                        ((Number) structure.get("base")).doubleValue() : 0.0;
                totals.merge("totalTotal", base, Double::sum);
                counts.merge("totalCount", 1, Integer::sum);
        }

        //foreach(K,V)
        totals.forEach((gender, total) -> {
            int count = counts.getOrDefault("totalCount", 1);
            double average = total / count;

            Map<String, Object> entry = new HashMap<>();
            entry.put("amount", average);
            result.add(entry);
        });

        return result;
    }

}
