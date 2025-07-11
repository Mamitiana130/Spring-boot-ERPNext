package mamt.project.newapp.controller.importation;

import jakarta.servlet.http.HttpSession;
import mamt.project.newapp.service.importation.EmployeeImportService;
import mamt.project.newapp.service.importation.ImportService;
import mamt.project.newapp.service.importation.SalarySlipImportService;
import mamt.project.newapp.service.importation.SalaryStructureImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/import")
public class ImportController {
    private final EmployeeImportService employeeImportService;
    private final SalaryStructureImportService salaryStructureImportService;
    private final SalarySlipImportService salarySlipImportService;
    private final ImportService importService;

    public ImportController(EmployeeImportService employeeImportService,
                            SalaryStructureImportService salaryStructureImportService,
                            SalarySlipImportService salarySlipImportService,
                            ImportService importService) {
        this.employeeImportService = employeeImportService;
        this.salaryStructureImportService = salaryStructureImportService;
        this.salarySlipImportService = salarySlipImportService;
        this.importService = importService;
    }

    @GetMapping
    public String showForm(HttpSession session, Model model) {
        // Vérification de la session
        if (session.getAttribute("sid") == null) {
            return "redirect:/login";
        }
        return "import/import";
    }

    @PostMapping
    public String uploadFiles(HttpSession session,
                              @RequestParam("file1") MultipartFile file1,
                              @RequestParam("file2") MultipartFile file2,
                              @RequestParam(value = "file3", required = false) MultipartFile file3,
                              Model model) {

        // Vérification de la session
        String sessionId = (String) session.getAttribute("sid");
        if (sessionId == null) {
            return "redirect:/login";
        }

        // Vérification des fichiers obligatoires
        if (file1.isEmpty() || file2.isEmpty()) {
            model.addAttribute("message", "Les fichiers 1 et 2 sont obligatoires");
            model.addAttribute("messageType", "error");
            return "import/import";
        }

        try {
            // Préparation des données
            List<Map<String, Object>> employees = employeeImportService.prepareEmployee(file1);
            List<Map<String, Object>> salaryStructures = salaryStructureImportService.prepareSalaryStructure(file2);

            // Le troisième fichier est optionnel
            List<Map<String, Object>> salarySlips = file3 != null && !file3.isEmpty()
                    ? salarySlipImportService.prepareSalarySlip(file3)
                    : List.of();

            // Appel du service d'importation avec la session ID
            ResponseEntity<Map> response = importService.importData(sessionId, employees, salaryStructures, salarySlips);

            // Traitement de la réponse
            if (response.getStatusCode().is2xxSuccessful()) {
                model.addAttribute("message", "Importation réussie: " + response.getBody());
                model.addAttribute("messageType", "success");
            } else {
                model.addAttribute("message", "Erreur lors de l'importation: " + response.getBody());
                model.addAttribute("messageType", "error");
            }

            return "import/import";

        } catch (Exception e) {
            model.addAttribute("message", "Erreur lors de l'importation: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "import/import";
        }
    }
}