package se.storkforge.petconnect.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import se.storkforge.petconnect.dto.PostInputDTO;
import se.storkforge.petconnect.dto.PostResponseDTO;
import se.storkforge.petconnect.service.PostService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public List<PostResponseDTO> getAllPosts(Pageable pageable) {
        return postService.getAllPosts(pageable);
    }

    @GetMapping("/user/{username}")
    public List<PostResponseDTO> getUserPosts(@PathVariable String username) {
        return postService.getUserPosts(username);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_PREMIUM')")
    public PostResponseDTO createPost(@RequestPart("data") PostInputDTO dto,
                                      @RequestPart(value = "file", required = false) MultipartFile file,
                                      Principal principal) {
        return postService.createPost(dto, file, principal.getName());
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id, Principal principal) {
        postService.deletePost(id, principal.getName());
    }
}
