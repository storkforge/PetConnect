package se.storkforge.petconnect.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import se.storkforge.petconnect.entity.Role;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.service.UserService;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.util.List;

@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin")
public class AdminUserController {

    private final UserService userService;
    private final Environment environment;

    @Value("${spring.application.version:N/A}")
    private String appVersion;

    public AdminUserController(UserService userService, Environment environment) {
        this.userService = userService;
        this.environment = environment;
    }

    @GetMapping
    public String redirectToDashboard() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String viewAdminDashboard(Model model) {
        long totalUsers = userService.getTotalUsersCount();
        long premiumUsersCount = userService.getPremiumUsersCount();
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("premiumUsersCount", premiumUsersCount);
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String viewAllUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users/list";
    }

    @GetMapping("/users/create")
    public String createUserForm(Model model) {
        model.addAttribute("user", new User());
        return "admin/users/create";
    }

    @PostMapping("/users/create") // Denna metod hanterar POST-förfrågningar till /admin/users/create
    public String processCreateUserForm(@ModelAttribute @Valid User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            // Om det finns valideringsfel, skicka tillbaka till formuläret med felmeddelanden
            return "admin/users/create";
        }
        userService.createUser(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/save") // Du har redan en metod för /users/save, kan behöva justeras eller tas bort
    public String saveUser(@ModelAttribute User user) {
        userService.save(user); // Använder nu userService.save(user)
        return "redirect:/admin/users";
    }

    @GetMapping("/users/premium")
    public String managePremiumUsers(Model model) {
        List<User> premiumUsers = userService.findUsersByRole("ROLE_PREMIUM");
        model.addAttribute("premiumUsers", premiumUsers);
        return "admin/users/premium";
    }

    @PostMapping("/users/{id}/togglePremium")
    public String togglePremium(@PathVariable Long id) {
        userService.togglePremiumRole(id);
        return "redirect:/admin/users/premium";
    }

    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        List<Role> allRoles = userService.getAllRoles();
        model.addAttribute("user", user);
        model.addAttribute("allRoles", allRoles);
        return "admin/users/edit"; // Namnet på din nya vy
    }

    // Observera att th:action i ditt formulär nu pekar på /admin/users/edit/{id}
    // Vi behöver en POST-metod för att hantera uppdateringen
    @PostMapping("/users/edit/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user) {
        userService.updateUser(id, user); // Se till att din updateUser-metod hanterar roller
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}")
    public String viewUserDetails(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id); // Använder nu den cachade getUserById
        model.addAttribute("user", user);
        return "admin/users/details";
    }

    @GetMapping("/roles")
    public String viewAllRoles(Model model) {
        List<Role> roles = userService.getAllRoles();
        model.addAttribute("roles", roles);
        return "admin/roles/list";
    }

    @GetMapping("/roles/create")
    public String createRoleForm(Model model) {
        model.addAttribute("role", new Role());
        return "admin/roles/create";
    }

    @PostMapping("/roles/save")
    public String saveRole(@ModelAttribute Role role) {
        userService.createRole(role);
        return "redirect:/admin/roles";
    }

    @GetMapping("/settings")
    public String viewSystemSettings(Model model) {
        model.addAttribute("appVersion", appVersion);
        model.addAttribute("databaseUrl", environment.getProperty("spring.datasource.url", "N/A"));
        model.addAttribute("databaseUsername", environment.getProperty("spring.datasource.username", "N/A"));
        return "admin/settings/view";
    }
}