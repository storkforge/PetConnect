package se.storkforge.petconnect.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
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

    /**
     * Retrieves a paginated list of all posts in the system.
     *
     * @param page the page number to retrieve (starting from 0). Defaults to 0 if not specified.
     * @param size the number of posts per page. Defaults to 10 if not specified.
     * @return ResponseEntity containing a list of PostResponseDTO representing the posts.
     */
    @GetMapping("/all")
    public ResponseEntity<List<PostResponseDTO>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<PostResponseDTO> posts = postService.getAllPosts(PageRequest.of(page, size));
        return ResponseEntity.ok(posts);
    }


    /**
     * Retrieves all posts made by a specific user.
     *
     * @param username The username of the post author
     * @return List of post DTOs by the given user
     */
    @GetMapping("/user/{username}")
    public List<PostResponseDTO> getUserPosts(@PathVariable String username) {
        return postService.getUserPosts(username);
    }

    /**
     * Creates a new post with optional image.
     *
     * @param dto       The post content data
     * @param principal The currently authenticated user
     * @return The created post DTO
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_PREMIUM')")
    public PostResponseDTO createPost(@RequestBody PostInputDTO dto,
                                      Principal principal) {
        return postService.createPost(dto, null, principal.getName());
    }

    /**
     * Deletes a post by its ID if the current user is the owner.
     *
     * @param id        The ID of the post to delete
     * @param principal The currently authenticated user
     */
    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id, Principal principal) {
        postService.deletePost(id, principal.getName());
    }
}
