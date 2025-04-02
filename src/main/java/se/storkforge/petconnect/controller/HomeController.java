package se.storkforge.petconnect.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("pageTitle", "Home");
        model.addAttribute("content", "index :: content");
        return "layout";
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("pageTitle", "Welcome");
        return "layout"; // layout.html will include the fragment from index.html
    }
}