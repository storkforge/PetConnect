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
        model.addAttribute("id", currentUser.getId());
        return "editProfile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("user") @Valid User userUpdate,
                                BindingResult result,
                                @RequestParam("file") MultipartFile file,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {


        User currentUser = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String username = currentUser.getUsername();

        if (!file.isEmpty())
            userService.uploadProfilePicture(currentUser.getId(), file);

        userService.updateUser(currentUser.getId(), userUpdate);

        redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        return "redirect:/profile/" + currentUser.getUsername();
    }
}