package mamt.project.newapp.controller;
import jakarta.servlet.http.HttpSession;
import mamt.project.newapp.repository.SalaryComponentRepository;
import mamt.project.newapp.repository.SalaryStructureRepository;
import mamt.project.newapp.service.ExoService;
import mamt.project.newapp.service.SalaireService;
import mamt.project.newapp.service.CRUDSalaireService;

import mamt.project.newapp.service.UpdateSalaireBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/crud-salaire")
public class CRUDSalaireController {
    @Autowired
    private UpdateSalaireBaseService updateSalaireBaseService;
    @Autowired
    private ExoService exoService;

    @Autowired
    private SalaireService salaireService;

    @Autowired
    private CRUDSalaireService crudSalaireService;

    private final SalaryStructureRepository structureRepository;
    private final SalaryComponentRepository componentRepository;
    private final Connection erpNextDbConnection;

    public CRUDSalaireController(SalaryStructureRepository structureRepository,
                                      SalaryComponentRepository componentRepository,
                                      Connection erpNextDbConnection) {
        this.structureRepository = structureRepository;
        this.componentRepository = componentRepository;
        this.erpNextDbConnection = erpNextDbConnection;
    }



    @GetMapping("/list-salary-assignment")
    public String manageSalariesAssignment(HttpSession session, Model model) {
        String sessionId = (String) session.getAttribute("sid");
        if (sessionId == null) return "redirect:/";

        List<Map<String, Object>> assigns = crudSalaireService.getSalaryStructureAssignments(sessionId);
        List<Map<String, Object>> alaryComponents= updateSalaireBaseService.getSalaryComponents(sessionId);
        model.addAttribute("assigns", assigns);

        return "crud/list_salary_assignment";
    }

    @GetMapping("/nouveau")
    public String showCreationForm(HttpSession session, Model model) {
        String sessionId = (String) session.getAttribute("sid");
        if (sessionId == null) return "redirect:/";

        List<Map<String, Object>> employees = crudSalaireService.getEmployees(sessionId);

        model.addAttribute("employees", employees);
        model.addAttribute("today", LocalDate.now().toString());

        return "crud/salary_form";
    }

    @PostMapping("/creer")
    public String createSalaryStructureAssignment(HttpSession session,
                                   @RequestParam String employee,
                                   @RequestParam(required = false) Double baseAmount,
                                   @RequestParam String startDate,
                                  @RequestParam String endDate,
                                  @RequestParam(required = false) String salaireEcraser,
                                   @RequestParam(required = false) Boolean salaireMoyenne,
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

            Map<String,String> filters = new HashMap<>();
            filters.put("employee",employeeId);
            filters.put("from_date",startDate);

            List<Map<String, Object>> salaryStructureAssignments = crudSalaireService.getSalaryStructureAssignmentsWithFilters(sessionId,filters);
            if (salaryStructureAssignments.isEmpty()){
                throw new RuntimeException("Pas encore de date de salaire base avant le reference");
            }

            Map<String,Object> lastSalaryStructureAssignment = salaryStructureAssignments.get(salaryStructureAssignments.size()-1);
            if (baseAmount==null){
                baseAmount = (double)lastSalaryStructureAssignment.get("base");
            }

            if (baseAmount<=0){
                throw new RuntimeException("Base Amount doit etre positif");
            }
            if (salaireMoyenne!=null){
                List<Map<String, Object>> existingAssignment = updateSalaireBaseService.getSalaryAssignments(sessionId);
                List<Map<String, Object>> mapbaseAmount=exoService.getAllAverage(existingAssignment);
                for (Map<String, Object>singleExistingAssignment:mapbaseAmount){
                    System.out.println("moyenne:"+singleExistingAssignment.get("amount"));
                    baseAmount=(double)singleExistingAssignment.get("amount");
                }
            }

            if (salaireEcraser!=null){
                //NANISY ECRASER
                String salaryStructure= (String)lastSalaryStructureAssignment.get("salary_structure");
                if (endDate != null && !endDate.isEmpty()) {

                    if (LocalDate.parse(startDate).isAfter(LocalDate.parse(endDate))) {
                        throw new RuntimeException("La date de début ne peut pas être après la date de fin.");
                    }

                    LocalDate startDateParsed = LocalDate.parse(startDate);
                    LocalDate endDateParsed = LocalDate.parse(endDate);

                    //AnneeMois
                    YearMonth startMonth = YearMonth.from(startDateParsed);
                    YearMonth endMonth = YearMonth.from(endDateParsed);

                    YearMonth current = startMonth;
                    while (!current.isAfter(endMonth)) {//false: on sort de la boucle
                        LocalDate fromDate = current.atDay(1);
                        LocalDate toDate = current.atEndOfMonth();

                        //Mijery oe miexiste ve ilay fiscal year
                        String year = String.valueOf(fromDate.getYear());
                        boolean fiscalExists = crudSalaireService.fiscalYearExists(sessionId, year);
                        if (!fiscalExists) {
                            Map<String, Object> fiscalResult = crudSalaireService.createFiscalYear(sessionId, year);
                            if (fiscalResult.containsKey("error")) {
                                throw new RuntimeException("Erreur lors de la création de l'année fiscale : " + fiscalResult.get("error"));
                            }
                        }

                        List<Map<String, Object>> existingAssignment = crudSalaireService.getSalaryStructuresAssignmentEmployeeFromDate(sessionId, fromDate, employeeId);
                        for (Map<String, Object>singleExistingAssignment:existingAssignment){
                            updateSalaireBaseService.cancelDocument(sessionId, "Salary Structure Assignment", (String)singleExistingAssignment.get("name"));
                            Map<String, Object> assignmentResult = crudSalaireService.createSalaryStructureAssignment(
                                    sessionId, employeeId, employeeName, salaryStructure, baseAmount, fromDate);

                            if (assignmentResult.containsKey("error")) {
                                throw new RuntimeException((String) assignmentResult.get("error"));
                            }
                            structureRepository.updateDateEmployee(erpNextDbConnection, employeeId,fromDate.toString());

                            crudSalaireService.createSalarySlip(sessionId, employeeId, employeeName, salaryStructure,
                                    fromDate, toDate, fromDate);
                        }

//                        if (existingAssignment.isEmpty()) {
//                            Map<String, Object> assignmentResult = crudSalaireService.createSalaryStructureAssignment(
//                                    sessionId, employeeId, employeeName, salaryStructure, baseAmount, fromDate);
//
//                            if (assignmentResult.containsKey("error")) {
//                                throw new RuntimeException((String) assignmentResult.get("error"));
//                            }
//
//                            crudSalaireService.createSalarySlip(sessionId, employeeId, employeeName, salaryStructure,
//                                    fromDate, toDate, fromDate);
//                        }

                        current = current.plusMonths(1);
                    }
                }
//                else {
//                    LocalDate startDateParsed = LocalDate.parse(startDate);
//                    YearMonth yearMonthStartDate = YearMonth.from(startDateParsed);
//                    LocalDate toDateParsed = yearMonthStartDate.atEndOfMonth();
//
//                    //Mijery oe miexiste ve ilay fiscal year
//                    String year = String.valueOf(startDateParsed.getYear());
//                    boolean fiscalExists = crudSalaireService.fiscalYearExists(sessionId, year);
//                    if (!fiscalExists) {
//                        Map<String, Object> fiscalResult = crudSalaireService.createFiscalYear(sessionId, year);
//                        if (fiscalResult.containsKey("error")) {
//                            throw new RuntimeException("Erreur lors de la création de l'année fiscale : " + fiscalResult.get("error"));
//                        }
//                    }
//                    Map<String, Object> assignmentResult = crudSalaireService.createSalaryStructureAssignment(
//                            sessionId, employeeId, employeeName, salaryStructure, baseAmount, startDateParsed);
//                    if (assignmentResult.containsKey("error")) {
//                        throw new RuntimeException((String) assignmentResult.get("error"));
//                    }
//
//                    crudSalaireService.createSalarySlip(sessionId, employeeId, employeeName, salaryStructure,
//                            startDateParsed, toDateParsed, startDateParsed);
//                }
            }else {
                //TSY NANISY
                String salaryStructure= (String)lastSalaryStructureAssignment.get("salary_structure");
                if (endDate != null && !endDate.isEmpty()) {

                    if (LocalDate.parse(startDate).isAfter(LocalDate.parse(endDate))) {
                        throw new RuntimeException("La date de début ne peut pas être après la date de fin.");
                    }

                    LocalDate startDateParsed = LocalDate.parse(startDate);
                    LocalDate endDateParsed = LocalDate.parse(endDate);

                    //AnneeMois
                    YearMonth startMonth = YearMonth.from(startDateParsed);
                    YearMonth endMonth = YearMonth.from(endDateParsed);

                    YearMonth current = startMonth;
                    while (!current.isAfter(endMonth)) {//false: on sort de la boucle
                        LocalDate fromDate = current.atDay(1);
                        LocalDate toDate = current.atEndOfMonth();

                        //Mijery oe miexiste ve ilay fiscal year
                        String year = String.valueOf(fromDate.getYear());
                        boolean fiscalExists = crudSalaireService.fiscalYearExists(sessionId, year);
                        if (!fiscalExists) {
                            Map<String, Object> fiscalResult = crudSalaireService.createFiscalYear(sessionId, year);
                            if (fiscalResult.containsKey("error")) {
                                throw new RuntimeException("Erreur lors de la création de l'année fiscale : " + fiscalResult.get("error"));
                            }
                        }

                        List<Map<String, Object>> existingAssignment = crudSalaireService.getSalaryStructuresAssignmentEmployeeFromDate(sessionId, fromDate, employeeId);

                        if (existingAssignment.isEmpty()) {
                            Map<String, Object> assignmentResult = crudSalaireService.createSalaryStructureAssignment(
                                    sessionId, employeeId, employeeName, salaryStructure, baseAmount, fromDate);

                            if (assignmentResult.containsKey("error")) {
                                throw new RuntimeException((String) assignmentResult.get("error"));
                            }


                            crudSalaireService.createSalarySlip(sessionId, employeeId, employeeName, salaryStructure,
                                    fromDate, toDate, fromDate);
                        }

                        current = current.plusMonths(1);
                    }
                } else {
                    LocalDate startDateParsed = LocalDate.parse(startDate);
                    YearMonth yearMonthStartDate = YearMonth.from(startDateParsed);
                    LocalDate toDateParsed = yearMonthStartDate.atEndOfMonth();

                    //Mijery oe miexiste ve ilay fiscal year
                    String year = String.valueOf(startDateParsed.getYear());
                    boolean fiscalExists = crudSalaireService.fiscalYearExists(sessionId, year);
                    if (!fiscalExists) {
                        Map<String, Object> fiscalResult = crudSalaireService.createFiscalYear(sessionId, year);
                        if (fiscalResult.containsKey("error")) {
                            throw new RuntimeException("Erreur lors de la création de l'année fiscale : " + fiscalResult.get("error"));
                        }
                    }
                    Map<String, Object> assignmentResult = crudSalaireService.createSalaryStructureAssignment(
                            sessionId, employeeId, employeeName, salaryStructure, baseAmount, startDateParsed);
                    if (assignmentResult.containsKey("error")) {
                        throw new RuntimeException((String) assignmentResult.get("error"));
                    }

                    crudSalaireService.createSalarySlip(sessionId, employeeId, employeeName, salaryStructure,
                            startDateParsed, toDateParsed, startDateParsed);
                }
            }




            redirectAttributes.addFlashAttribute("success", "Bulletin de salaire créé avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création: " + e.getMessage());
        }
        return "redirect:/crud-salaire/nouveau";

    }

    @GetMapping("/edit")
    public String showEditForm(@RequestParam(required = false) String id, HttpSession session, Model model) {
        String sessionId = (String) session.getAttribute("sid");
        if (sessionId == null) return "redirect:/";

        try {
            System.out.println("atoooooo");
            Map<String, Object> salaryAssignment = crudSalaireService.getSalaryStructureAssignmentById(sessionId, id);

            if (salaryAssignment == null || salaryAssignment.containsKey("error")) {
                model.addAttribute("error", "Bulletin de salaire non trouvé");
                return "redirect:/crud-salaire";
            }

            List<Map<String, Object>> employees = crudSalaireService.getEmployees(sessionId);
            List<Map<String, Object>> salaryStructures = crudSalaireService.getSalaryStructures(sessionId);

            model.addAttribute("salaryAssignment", salaryAssignment);
            model.addAttribute("employees", employees);
            model.addAttribute("salaryStructures", salaryStructures);
            model.addAttribute("isEdit", true);
            model.addAttribute("assignId", id);

            return "crud/salary_form";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement: " + e.getMessage());
            return "redirect:/crud-salaire";
        }
    }
/*===================================================alea anticipe=================================================*/
    @PostMapping("/update")
    public String updateSalarySlip(@RequestParam String assignId,
                                   HttpSession session,
                                   @RequestParam String employee,
                                   @RequestParam String salaryStructure,
                                   @RequestParam Double baseAmount,
                                   @RequestParam String startDate,
                                   @RequestParam String endDate,
                                   @RequestParam String postingDate,
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

            LocalDate startLocalDate = LocalDate.parse(startDate);
            LocalDate endLocalDate = LocalDate.parse(endDate);
            LocalDate postingLocalDate = LocalDate.parse(postingDate);

            Map<String, Object> result = crudSalaireService.updateSalaryAssignmentAndSlip(
                    sessionId, assignId, employeeId, employeeName, salaryStructure,
                    startLocalDate, endLocalDate, postingLocalDate, baseAmount);

            if (result.containsKey("error")) {
                throw new RuntimeException((String) result.get("error"));
            }

            model.addAttribute("success", "Bulletin de salaire mis à jour avec succès !");
            return "redirect:/crud-salaire/list-salary-assignment";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
            return showEditForm(assignId, session, model);
        }
    }

    @GetMapping("/recherche")
    public String manageSalaries(HttpSession session, Model model) {
        String sessionId = (String) session.getAttribute("sid");
        if (sessionId == null) return "redirect:/";

        Map<String, String> filters = new HashMap<>();
        filters.put("docstatus","1");

        // Récupérer la liste des salaires existants
        List<Map<String, Object>> slips = salaireService.getSalarySlips(sessionId, filters);
        List<Map<String, Object>> salaryComponents= updateSalaireBaseService.getSalaryComponents(sessionId);
        model.addAttribute("salaryComponents", salaryComponents);
        model.addAttribute("slips", slips);

        return "crud/list_salary_slip";
    }
    @GetMapping("/salaires-employe-recherche")
    public String updateSalarySlip(HttpSession session,
                                   @RequestParam(required = false) String condition,
                                   @RequestParam String montant,
                                   @RequestParam String component,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        String sessionId = (String) session.getAttribute("sid");
        if (sessionId == null) return "redirect:/";

        try {

            Double montantValue = Double.parseDouble(montant);

            List<Map<String, Object>> matchingSlips = updateSalaireBaseService.getSalarySlipsWithCondition(
                    sessionId, component, montantValue, condition);
            List<Map<String, Object>> salaryComponents= updateSalaireBaseService.getSalaryComponents(sessionId);
            model.addAttribute("salaryComponents", salaryComponents);

            model.addAttribute("slips", matchingSlips);

            return "crud/list_salary_slip";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
        }
        return "crud/list_salary_slip";

    }


}
