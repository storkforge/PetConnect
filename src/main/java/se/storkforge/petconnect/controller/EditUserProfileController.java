package se.storkforge.petconnect.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.service.PetService;
import se.storkforge.petconnect.service.UserService;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/settings")
public class EditUserProfileController {
    private final UserService userService;
    private final PetService petService;

    public EditUserProfileController(UserService userService, PetService petService) {
        this.userService = userService;
        this.petService = petService;
    }

    @GetMapping("/profile")
    public String editProfileForm(Principal principal, Model model) {
        String username = principal.getName();
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        model.addAttribute("user", currentUser);
        return "editProfile";
    }

    @PostMapping("/profile")
    public String updateUserProfile(Principal principal,
                                    @ModelAttribute("user") User updatedUser,
                                    @RequestParam(value = "file", required = false) MultipartFile file,
                                    RedirectAttributes redirectAttributes) {
        try {
            String username = principal.getName();
            User currentUser = userService.getUserByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            currentUser.setUsername(updatedUser.getUsername());
            currentUser.setEmail(updatedUser.getEmail());
            currentUser.setPhoneNumber(updatedUser.getPhoneNumber());

            userService.save(currentUser);

            if (file != null && !file.isEmpty()) {
                userService.uploadProfilePicture(currentUser.getId(), file);
            }

            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
        }

        return "redirect:/settings/profile";
    }

    @GetMapping("/profile/view")
    public String viewProfile(Principal principal, Model model) {
        String username = principal.getName();
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Pet> pets = petService.getPetsByUser(currentUser.getId());

        model.addAttribute("user", currentUser);
        model.addAttribute("pets", pets);
        return "profileView";
    }
}
