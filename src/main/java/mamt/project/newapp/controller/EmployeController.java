package mamt.project.newapp.controller;

import jakarta.servlet.http.HttpSession;
import mamt.project.newapp.service.EmployeService;
import mamt.project.newapp.service.SalaireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/employe")
public class EmployeController {

    @Autowired
    private EmployeService employeService;
    @Autowired
    private SalaireService salaireService;


    @GetMapping
    public String listEmployes(HttpSession session,
                               Model model,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String department,
                               @RequestParam(required = false) String gender,
                               @RequestParam(required = false) String fromBirthDate,
                               @RequestParam(required = false) String toBirthDate) {

        String sessionId = (String) session.getAttribute("sid");
        if (sessionId == null) return "redirect:/";

        Map<String, String> filters = new HashMap<>();
        if (status != null && !status.isEmpty()) {
            filters.put("status", status);
        }
        if (department != null && !department.isEmpty()) {
            filters.put("department", department);
        }
        if (gender != null && !gender.isEmpty()) {
            filters.put("gender", gender);
        }
        if (fromBirthDate != null && !fromBirthDate.isEmpty() &&
                toBirthDate != null && !toBirthDate.isEmpty()) {
            filters.put("birth_date_range", fromBirthDate + "," + toBirthDate);
        }

        model.addAttribute("employes", employeService.getEmployes(sessionId, filters));
        model.addAttribute("currentFilters", Map.of(
                "status", status != null ? status : "",
                "gender", gender != null ? gender : "",
                "fromBirthDate", fromBirthDate != null ? fromBirthDate : "",
                "toBirthDate", toBirthDate != null ? toBirthDate : ""
        ));

        return "employe/list";
    }

    /*==========================================Details de l employee(d)=====================================================*/


    @GetMapping("/{employeeId}")
    public String showEmployeeDetails(@PathVariable String employeeId,
                                      HttpSession session,
                                      Model model) {
        String sessionId = (String) session.getAttribute("sid");
        if (sessionId == null) return "redirect:/";

        Map<String, Object> employeeDetails = employeService.getEmployeeDetails(sessionId, employeeId);

        if (employeeDetails.isEmpty()) {
            return "redirect:/employe";
        }

        List<Map<String, Object>> salaires = salaireService.getSalairesByEmployee(sessionId, employeeId);

        model.addAttribute("employee", employeeDetails);
        model.addAttribute("salaires", salaires);

        return "employe/details";
    }


}