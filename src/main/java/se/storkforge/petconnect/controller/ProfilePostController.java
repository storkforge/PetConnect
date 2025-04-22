package se.storkforge.petconnect.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import se.storkforge.petconnect.dto.PostInputDTO;
import se.storkforge.petconnect.service.PostService;

import java.security.Principal;

@Controller
public class ProfilePostController {

    private final PostService postService;

    public ProfilePostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/profile/{username}/post")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_PREMIUM')")
    public String handleProfilePost(@PathVariable String username,
                                    @RequestParam("content") String content,
                                    @RequestParam(value = "file", required = false) MultipartFile file,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes,
                                    HttpServletRequest request) {
        if (!principal.getName().equals(username)) {
            redirectAttributes.addFlashAttribute("error", "You are not allowed to post as another user.");
            return "redirect:/profile/" + username;
        }

        PostInputDTO dto = new PostInputDTO();
        dto.setContent(content);

        try {
            postService.createPost(dto, file, username);
            redirectAttributes.addFlashAttribute("success", "Post created successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile/" + username;
    }
}
