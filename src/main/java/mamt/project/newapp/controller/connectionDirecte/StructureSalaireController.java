package mamt.project.newapp.controller.connectionDirecte;

import mamt.project.newapp.repository.SalaryComponentRepository;
import mamt.project.newapp.repository.SalaryStructureRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/salary-structures")
public class StructureSalaireController {

    private final SalaryStructureRepository structureRepository;
    private final SalaryComponentRepository componentRepository;
    private final Connection erpNextDbConnection;

    public StructureSalaireController(SalaryStructureRepository structureRepository,
                                      SalaryComponentRepository componentRepository,
                                      Connection erpNextDbConnection) {
        this.structureRepository = structureRepository;
        this.componentRepository = componentRepository;
        this.erpNextDbConnection = erpNextDbConnection;
    }
    /*===============================================Read=======================================================*/

    @GetMapping
    public String listSalaryStructures(Model model) {
        try {
            List<Map<String, Object>> structures = structureRepository.getAllSalaryStructures(erpNextDbConnection);
            model.addAttribute("salaryStructures", structures);
        } catch (SQLException e) {
            model.addAttribute("error", "Erreur lors de la récupération des structures: " + e.getMessage());
        }
        return "directe/list_salary_structure";
    }

    @GetMapping("/details/{name}")
    public String getStructureComponents(@PathVariable String name, Model model) {
        try {
            Map<String, Object> structure = structureRepository.getByName(erpNextDbConnection, name);
            if (structure == null) {
                model.addAttribute("error", "Structure de salaire non trouvée");
                return "directe/details_salary_structure";
            }

            List<Map<String, Object>> components = componentRepository.getComponentsByStructure(erpNextDbConnection, name);
            structure.put("components", components);
            model.addAttribute("structure", structure);

        } catch (SQLException e) {
            model.addAttribute("error", "Erreur technique: " + e.getMessage());
        }
        return "directe/details_salary_structure";
    }
    /*===============================================Create=======================================================*/
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("editMode", false);
        return "directe/form_salary_structure";
    }

    @PostMapping("/add")
    public String addSalaryStructure(@RequestParam String name,
                                     @RequestParam String company,
                                     @RequestParam String currency,
                                     @RequestParam String payroll_frequency,
                                     Model model) {
        try {
            structureRepository.insertSalaryStructure(erpNextDbConnection, name, company, currency, payroll_frequency);
            model.addAttribute("message", "Structure de salaire ajoutée avec succès !");
        } catch (SQLException e) {
            model.addAttribute("message", "Erreur lors de l'insertion : " + e.getMessage());
        }
        return "directe/form_salary_structure";
    }

    /*===============================================Update=======================================================*/


    @GetMapping("/edit/{name}")
    public String editSalaryStructure(@PathVariable String name, Model model) {
        try {
            Map<String, Object> structure = structureRepository.getByName(erpNextDbConnection, name);
            if (structure == null) {
                model.addAttribute("error", "Structure de salaire non trouvée");
                return "redirect:/salary-structures";
            }
            model.addAttribute("structure", structure);
            model.addAttribute("editMode", true);
        } catch (SQLException e) {
            model.addAttribute("error", "Erreur technique : " + e.getMessage());
            return "redirect:/salary-structures";
        }
        return "directe/form_salary_structure";
    }

    @PostMapping("/edit")
    public String updateSalaryStructure(@RequestParam String name,
                                        @RequestParam String company,
                                        @RequestParam String currency,
                                        @RequestParam String payroll_frequency,
                                        Model model) {
        try {
            structureRepository.updateSalaryStructure(erpNextDbConnection, name, company, currency, payroll_frequency);
            model.addAttribute("success", "Structure de salaire mise à jour avec succès !");
        } catch (SQLException e) {
            model.addAttribute("error", "Erreur lors de la mise à jour : " + e.getMessage());
        }
        return "redirect:/salary-structures";
    }


}