package mamt.project.newapp.controller;


import jakarta.servlet.http.HttpSession;
import mamt.project.newapp.service.ResetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reset")
public class ResetController {
    private final ResetService resetService;

    public ResetController(ResetService resetService) {
        this.resetService = resetService;
    }

    @GetMapping
    public String index() {
        return "reset/reset";
    }
    @GetMapping("/resetBase")
    public String resetBase(Model model, HttpSession session) {
        String sessionId = (String) session.getAttribute("sid");
        if (sessionId == null) {
            return "redirect:/";
        }
        resetService.reset(sessionId);
        model.addAttribute("message","Base de donnees nettoye");
        return "reset/reset";
    }
}
