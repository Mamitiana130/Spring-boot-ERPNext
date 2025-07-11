package mamt.project.newapp.controller;

import jakarta.servlet.http.HttpSession;
import mamt.project.newapp.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/exo")
public class ExoController {
    @Autowired
    private SalaireService salaireService;
    @Autowired
    private CRUDSalaireService crudSalaireService;
    @Autowired
    private EmployeService employeService;
    @Autowired
    private ExoService exoService;

    @GetMapping("/list-salary-sexe")
    public String listSalarySexe(HttpSession session,
                                    @RequestParam(required = false) String annee,
                                    Model model) {
        String sessionId = (String) session.getAttribute("sid");
        if (sessionId == null) return "redirect:/";

        Map<String, String> filters = new HashMap<>();
        if (annee != null && !annee.isEmpty()) filters.put("annee", annee);
        filters.put("docstatus","1");

        List<Map<String, Object>> slips = salaireService.getSalarySlips(sessionId, filters);
        List<Map<String, Object>> employees = employeService.getEmployes(sessionId,new HashMap<>());
        List<Map<String, Object>> slipsEmployeesAverages = exoService.getSalarySlipsEmployeesAverages(slips,employees);


        model.addAttribute("slipsEmployeesAverages", slipsEmployeesAverages);
        model.addAttribute("currentYear", annee != null ? annee : "");


        return "exo/list_salary_sexe";
    }

    /*=========================================================================================================================*/
    @GetMapping("/create-salary-assignment-date")
    public String createSalaryDate(HttpSession session,
                                   @RequestParam(required = false) String success,
                                   @RequestParam(required = false) String error,
                                    Model model) {
        String sessionId = (String) session.getAttribute("sid");
        if (sessionId == null) return "redirect:/";
        List<Map<String, Object>> structures = crudSalaireService.getSalaryStructures(sessionId);

        List<Map<String, Object>> employees = employeService.getEmployes(sessionId,new HashMap<>());

        model.addAttribute("employees", employees);
        model.addAttribute("salary_structures", structures);

        return "exo/form_salary_date";
    }

    @PostMapping("/create-salary-assignment-date")
    public String createSalaryStructureAssignment(HttpSession session,
                                                  @RequestParam String employee,
                                                  @RequestParam String salaryStructure,
                                                  @RequestParam List<Double> baseAmount,
                                                  @RequestParam List<Integer> annee,
                                                  @RequestParam List<Integer> mois,
                                                  RedirectAttributes redirectAttributes,
                                                  Model model) {
        String sessionId = (String) session.getAttribute("sid");
        if (sessionId == null) return "redirect:/";

        try {
            String[] employeeParts = employee.split("\\|");
            if (employeeParts.length != 2) {
                throw new RuntimeException("Format d'employé invalide");
            }
            String employeeId = employeeParts[0];
            String employeeName = employeeParts[1];
            for (int i = 0; i < baseAmount.size(); i++) {
                LocalDate dateParse = LocalDate.of(annee.get(i), mois.get(i), 1);
                Double singleBaseAmount = baseAmount.get(i);
                if (singleBaseAmount<=0){
                    throw new RuntimeException("Base Amount doit etre positif");
                }
                Map<String, Object> assignmentResult = crudSalaireService.createSalaryStructureAssignment(
                        sessionId, employeeId, employeeName, salaryStructure, baseAmount.get(i), dateParse);

                if (assignmentResult.containsKey("error")) {
                    throw new RuntimeException((String) assignmentResult.get("error"));
                }
            }

            redirectAttributes.addFlashAttribute("success", "Salary structure assignment créé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création: " + e.getMessage());
        }

        return "redirect:/exo/create-salary-assignment-date";
    }
}
