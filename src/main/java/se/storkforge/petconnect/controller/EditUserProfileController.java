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
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.service.UserService;

@Controller
@RequestMapping("/settings")
public class EditUserProfileController {
    private final UserService userService;

    public EditUserProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String editProfileForm(@AuthenticationPrincipal UserDetails userDetails,
                                  Model model) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        model.addAttribute("user", currentUser);
        return "editProfile";
    }

    @PostMapping("/profile")
    public String updateUserProfile(@AuthenticationPrincipal UserDetails userDetails,
                                    @ModelAttribute("user") User updatedUser,
                                    @RequestParam(value = "file", required = false) MultipartFile file,
                                    RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Update basic user details
            currentUser.setUsername(updatedUser.getUsername());
            currentUser.setEmail(updatedUser.getEmail());
            currentUser.setPhoneNumber(updatedUser.getPhoneNumber());

            // Save updated user fields
            userService.save(currentUser);

            // Update profile picture only if file is present and not empty
            if (file != null && !file.isEmpty()) {
                userService.uploadProfilePicture(currentUser.getId(), file);
            }

            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
        }

        return "redirect:/settings/profile";
    }
}
