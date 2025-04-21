package se.storkforge.petconnect.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.service.PetService;
import se.storkforge.petconnect.service.UserService;
import se.storkforge.petconnect.service.storageService.FileStorageService;

import java.util.List;

@Controller
@RequestMapping("/profile")
public class UserProfileController {

    private final UserService userService;
    private final PetService petService;
    private final FileStorageService fileStorageService;

    public UserProfileController(UserService userService,
                                 PetService petService,
                                 FileStorageService fileStorageService) {
        this.userService = userService;
        this.petService = petService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/{username}")
    public String viewProfile(@PathVariable String username,
                               Model model,
                              @AuthenticationPrincipal UserDetails currentUser) {

        User profileUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean isOwner = currentUser != null &&
                currentUser.getUsername().equals(username);



        model.addAttribute("user", profileUser);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("pets", petService.getPetsByOwner(profileUser));

        return "profileView";
    }
}