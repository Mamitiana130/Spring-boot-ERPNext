package mamt.project.newapp.controller;

import jakarta.servlet.http.HttpSession;
import mamt.project.newapp.service.ERPNextAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
    @Autowired
    private ERPNextAuthService authService;

    @GetMapping("/")
    public String loginForm() {
        return "login/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        Model model,
                        HttpSession session) {
        try {
            String message = authService.login(username, password);
            if (message != null && message.startsWith("sid=")) {
                session.setAttribute("sid", message);
                return "redirect:/accueil";//SupplierController
            } else {
                model.addAttribute("error", message);
                return "login/login";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Erreur : " + e.getMessage());
            return "login/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }


}