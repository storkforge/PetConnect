package se.storkforge.petconnect.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import se.storkforge.petconnect.dto.PostInputDTO;
import se.storkforge.petconnect.dto.PostResponseDTO;
import se.storkforge.petconnect.service.PostService;

import java.security.Principal;
import java.util.List;

@Controller
public class PostGraphQLController {

    private final PostService postService;

    public PostGraphQLController(PostService postService) {
        this.postService = postService;
    }

    @QueryMapping
    public List<PostResponseDTO> getAllPosts(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return postService.getAllPosts(PageRequest.of(page, size));
    }

    @QueryMapping
    public List<PostResponseDTO> getUserPosts(@Argument String username) {
        return postService.getUserPosts(username);
    }

    @MutationMapping
    public PostResponseDTO createPost(@Argument PostInputDTO dto, Principal principal) {
        return postService.createPost(dto, null, principal.getName()); // без файла
    }

    @MutationMapping
    public Boolean deletePost(@Argument Long id, Principal principal) {
        postService.deletePost(id, principal.getName());
        return true;
    }

}
