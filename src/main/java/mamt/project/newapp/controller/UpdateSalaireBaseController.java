package mamt.project.newapp.controller;

import jakarta.servlet.http.HttpSession;
import mamt.project.newapp.service.SalaireService;
import mamt.project.newapp.service.CRUDSalaireService;

import mamt.project.newapp.service.UpdateSalaireBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/update-salaire-base")
public class UpdateSalaireBaseController {
    @Autowired
    private UpdateSalaireBaseService updateSalaireBaseService;
    @Autowired
    private CRUDSalaireService crudSalaireService ;
    @Autowired
    private SalaireService salaireService ;
    /*===================================================Update salary Structure Assignment/Slip(ancien alea 2)=================================================*/
    @GetMapping("/form")
    public String showUpdateForm(HttpSession session, Model model) {
        String sessionId = (String) session.getAttribute("sid");
        if (sessionId == null) return "redirect:/";
        List<Map<String, Object>> salaryComponents= updateSalaireBaseService.getSalaryComponents(sessionId);
        model.addAttribute("salaryComponents", salaryComponents);

        return "salaire/update_salary_base_form";
    }

    @PostMapping("/update")
    public String updateSalarySlip(HttpSession session,
                                   @RequestParam String taux,
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

            for (Map<String, Object> matchingSlip : matchingSlips) {
                //1- Cancel salary structure slip(status: 2)
                    updateSalaireBaseService.cancelDocument(sessionId, "Salary Slip", (String)matchingSlip.get("name"));

                List<Map<String, Object>> assignments = updateSalaireBaseService.getSalaryAssignmentsForEmployee(
                        sessionId, (String) matchingSlip.get("employee"),(String)matchingSlip.get("start_date"));

                for (Map<String, Object> assignment: assignments){
                    //2- Cancel salary structure assignment(status: 2)
                    updateSalaireBaseService.cancelDocument(sessionId, "Salary Structure Assignment", (String)assignment.get("name"));

                    //3- Creation de nouveau salary structure assignment
                    LocalDate dateSalaire=LocalDate.parse((String)assignment.get("from_date"));
                    double montantAncien= (double) assignment.get("base");
                    if (montantAncien == 0) {
                        throw new RuntimeException("Le montant salaire structure assignment est 0");
                    }

                    double tauxDouble = Double.parseDouble(taux);
                    double montantNouveau =0;

                    if (tauxDouble == 0){
                        montantNouveau=montantAncien;
                    }else {
                        double valeurTaux= (montantAncien*tauxDouble)/100;
                        montantNouveau = montantAncien+valeurTaux;
                    }

                    crudSalaireService.createSalaryStructureAssignment(sessionId,(String)assignment.get("employee"),
                            (String)assignment.get("employee_name"),(String)assignment.get("salary_structure"),montantNouveau,dateSalaire);

                    //4-Creation des nouveux salary slip par ces nouveau salaire de base
                    LocalDate startDate=LocalDate.parse((String)matchingSlip.get("start_date"));
                    LocalDate endDate=LocalDate.parse((String)matchingSlip.get("end_date"));
                    LocalDate postingDate=LocalDate.parse((String)matchingSlip.get("posting_date"));

                    crudSalaireService.createSalarySlip(sessionId,(String)matchingSlip.get("employee"),
                            (String)matchingSlip.get("employee_name"),(String)matchingSlip.get("salary_structure"),
                                    startDate,endDate,postingDate);
//                    String from_date= (String)assignment.get("from_date");
//                    int mois= Integer.parseInt(from_date.substring(5, 7));
//                    int annees = Integer.parseInt(from_date.substring(0, 4));
//                    int lastDay = updateSalaireBaseService.getLastDay(annees,mois);
//                    LocalDate to_date = LocalDate.of(annees, mois, lastDay);

                }
            }

            redirectAttributes.addFlashAttribute("success",
                    matchingSlips.size() + " slips trouvés avec les critères spécifiés");
            return "redirect:/update-salaire-base/form";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
            return showUpdateForm(session, model);
        }
    }
}
