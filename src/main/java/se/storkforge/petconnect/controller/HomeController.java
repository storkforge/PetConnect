package se.storkforge.petconnect.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "Welcome");
        return "index";
    }


    @GetMapping("/homepage")
    public String index(Model model) {
        model.addAttribute("pageTitle", "Welcome");
        return "layout/layout"; // layout.html will include the fragment from index.html
    }

    @Controller
    public static class AuthController {

        @GetMapping("/login")
        public String loginPage() {
            return "auth/login"; // This maps to log in.html in templates
        }
    }
}