package mamt.project.newapp.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mamt.project.newapp.service.EmployeService;
import mamt.project.newapp.service.ExportService;
import mamt.project.newapp.service.SalaireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/export")
public class ExportController {

    @Autowired
    private ExportService exportService;

    @Autowired
    private SalaireService salaireService;

    @Autowired
    private EmployeService employeService;

    @GetMapping("/salaire-detail")
    public void exportSalairePdf(@RequestParam("id") String id,
                                 HttpSession session,
                                 HttpServletResponse response) throws IOException {
        System.out.println("Export ID: " + id); // Log pour vérification

        String sessionId = (String) session.getAttribute("sid");
        if (sessionId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Map<String, Object> salaire = salaireService.getSalaireDetails(sessionId, id);

        String employeeId = (String) salaire.get("employee");
        Map<String, Object> employee = employeService.getEmployeeDetails(sessionId, employeeId);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"fiche_paie_" + id + ".pdf\"");

        exportService.generateFichePaie(response.getOutputStream(), employee, salaire);
    }
}