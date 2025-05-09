package se.storkforge.petconnect.controller;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import se.storkforge.petconnect.entity.MeetUp;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.service.PetService;
import se.storkforge.petconnect.service.UserService;
import se.storkforge.petconnect.service.storageService.FileStorageService;
import java.security.Principal;
import java.util.Set;

@Controller
@RequestMapping("/profile")
public class UserProfileController {

    private final UserService userService;
    private final PetService petService;

    public UserProfileController(UserService userService,
                                 PetService petService) {
        this.userService = userService;
        this.petService = petService;
    }

    @GetMapping("/{username}")
    public String viewProfile(@PathVariable String username,
                              Principal principal,
                              Model model) {

        if (principal == null) {
            throw new UsernameNotFoundException("User must be logged in to view profiles");
        }

        User loggedInUser = userService.getUserByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Logged in user not found"));

        User profileUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Profile user not found"));

        boolean isOwner = loggedInUser.getId().equals(profileUser.getId());
        boolean isUser = loggedInUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_USER"));

        model.addAttribute("user", profileUser);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("isUser", isUser);
        model.addAttribute("pets", petService.getPetsByOwner(profileUser));

        return "profileView";
    }

    @GetMapping("/{username}/meetup")
    public String viewMeetup(@PathVariable String username,
                              Principal principal,
                              Model model) {

        if (principal == null) {
            throw new UsernameNotFoundException("User must be logged in to view profiles");
        }

        User loggedInUser = userService.getUserByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Logged in user not found"));

        User profileUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Profile user not found"));

        boolean isOwner = loggedInUser.getId().equals(profileUser.getId());
        Set<MeetUp> meetups = profileUser.getMeetUps();

        model.addAttribute("user", profileUser);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("meetups", meetups);

        return "viewMeetup";
    }

}