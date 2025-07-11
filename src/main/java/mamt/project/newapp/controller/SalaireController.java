package mamt.project.newapp.controller;

import jakarta.servlet.http.HttpSession;
import mamt.project.newapp.service.SalaireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/salaire")
public class SalaireController {

    @Autowired
    private SalaireService salaireService;


    /*==========================================All salaire employee filtre mois/annees(2-e)=====================================================*/

    @GetMapping("/salaires-mensuels-employe")
    public String salairesParMoisEmploye(HttpSession session,
                                  @RequestParam(required = false) String mois,
                                  @RequestParam(required = false) String annee,
                                         Model model) {
        String sessionId = (String) session.getAttribute("sid");
        if (sessionId == null) return "redirect:/";


        Map<String, String> filters = new HashMap<>();
        if (mois != null && !mois.isEmpty()) filters.put("mois", mois);
        if (annee != null && !annee.isEmpty()) filters.put("annee", annee);
        filters.put("docstatus","1");

        List<Map<String, Object>> slips = salaireService.getSalarySlips(sessionId, filters);

        salaireService.enrichirAvecComponents(sessionId, slips);

        double totalNetPays = slips.stream()
                .mapToDouble(slip -> slip.get("net_pay") instanceof Number ?
                        ((Number) slip.get("net_pay")).doubleValue() : 0.0)
                .sum();

        double totalGlobalDeductions = slips.stream()
                .mapToDouble(slip -> slip.get("total_deduction") instanceof Number ?
                        ((Number) slip.get("total_deduction")).doubleValue() : 0.0)
                .sum();

        double totalGlobalEarnings = slips.stream()
                .mapToDouble(slip -> slip.get("total_earnings_components") instanceof Number ?
                        ((Number) slip.get("total_earnings_components")).doubleValue() : 0.0)
                .sum();

        model.addAttribute("slips", slips);
        model.addAttribute("totalNetPays", totalNetPays);
        model.addAttribute("totalGlobalDeductions", totalGlobalDeductions);
        model.addAttribute("totalGlobalEarnings", totalGlobalEarnings); // Ajoutez cette ligne
        model.addAttribute("currentFilters", Map.of(
                "annee", annee != null ? annee : "",
                "mois", mois != null ? mois : ""
        ));
        return "salaire/list_mois_employe";
    }


    /*==========================================All salaire mensuel filtre annees(3-a)=====================================================*/

    @GetMapping("/salaires-mensuels")
    public String salairesParMois(HttpSession session,
                                  @RequestParam(required = false) String annee,
                                  Model model) {
        String sessionId = (String) session.getAttribute("sid");
        if (sessionId == null) return "redirect:/";

        Map<String, String> filters = new HashMap<>();
        if (annee != null && !annee.isEmpty()) filters.put("annee", annee);
        filters.put("docstatus","1");

        List<Map<String, Object>> slips = salaireService.getSalarySlips(sessionId, filters);
        List<Map<String, Object>> slipsMois = salaireService.enrichirAvecComponentsMois(sessionId, slips);

        // Calcul des totaux globaux
        double totalNetPays = slipsMois.stream()
                .mapToDouble(month -> month.get("total_net_pay") instanceof Number ?
                        ((Number) month.get("total_net_pay")).doubleValue() : 0.0)
                .sum();

        double totalGlobalEarnings = slipsMois.stream()
                .mapToDouble(month -> month.get("total_earnings")instanceof Number ?
                        ((Number) month.get("total_earnings")).doubleValue() : 0.0)
                .sum();

        double totalGlobalDeductions = slipsMois.stream()
                .mapToDouble(month -> month.get("total_deductions")instanceof Number ?
                        ((Number) month.get("total_deductions")).doubleValue() : 0.0)
                .sum();

        model.addAttribute("monthsData", slipsMois);
        model.addAttribute("totalNetPays", totalNetPays);
        model.addAttribute("totalGlobalEarnings", totalGlobalEarnings);
        model.addAttribute("totalGlobalDeductions", totalGlobalDeductions);
        model.addAttribute("currentYear", annee != null ? annee : "");

        return "salaire/statistic_mois";
    }

    /*==========================================Graphe(3-c)=====================================================*/
    @GetMapping("/evolution-salaires")
    public String evolutionSalaires(HttpSession session,
                                    @RequestParam(required = false) String annee,
                                    Model model) {
        String sessionId = (String) session.getAttribute("sid");
        if (sessionId == null) return "redirect:/";

        Map<String, String> filters = new HashMap<>();
        if (annee != null && !annee.isEmpty()) filters.put("annee", annee);
        filters.put("docstatus","1");

        List<Map<String, Object>> slips = salaireService.getSalarySlips(sessionId, filters);
        List<Map<String, Object>> monthlyData = salaireService.enrichirAvecComponentsMois(sessionId, slips);

        model.addAttribute("monthlyData", monthlyData);
        model.addAttribute("currentYear", annee != null ? annee : "");

        return "salaire/evolution_charts2";
    }
}