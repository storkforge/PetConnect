package se.storkforge.petconnect.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("content", "index :: content"); // refers to the fragment inside index.html
        model.addAttribute("pageTitle", "Home");
        return "layout"; // layout.html is the shell, index.html provides the inner content
    }
}