package se.storkforge.petconnect.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import se.storkforge.petconnect.dto.PostInputDTO;
import se.storkforge.petconnect.dto.PostResponseDTO;
import se.storkforge.petconnect.entity.Post;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.repository.PostRepository;
import se.storkforge.petconnect.repository.UserRepository;
import se.storkforge.petconnect.service.storageService.FileStorageService;
import se.storkforge.petconnect.util.OwnershipValidator;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final FileStorageService fileStorageService;
    private final OwnershipValidator ownershipValidator;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, FileStorageService fileStorageService,
                       OwnershipValidator validator, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.fileStorageService = fileStorageService;
        this.ownershipValidator = validator;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new post with optional image and associates it with the logged-in user.
     *
     * @param dto Data transfer object containing the content of the post
     * @param file Optional image file attached to the post (can be null)
     * @param username The username of the user creating the post
     * @return A PostResponseDTO representing the created post
     * @throws UsernameNotFoundException if the user is not found
     */
    public PostResponseDTO createPost(PostInputDTO dto, MultipartFile file, String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String imagePath = file != null && !file.isEmpty() ? fileStorageService.store(file, "posts") : null;

        Post post = new Post();
        post.setContent(dto.getContent());
        post.setImagePath(imagePath);
        post.setAuthor(author);
        postRepository.save(post);

        return toDto(post);
    }

    /**
     * Retrieves a paginated list of all posts from the database.
     *
     * @param pageable Pagination and sorting information
     * @return A list of PostResponseDTOs representing the posts
     */
    public List<PostResponseDTO> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(this::toDto)
                .toList();
    }

    /**
     * Retrieves all posts created by a specific user in reverse chronological order.
     *
     * @param username The username of the user whose posts should be retrieved
     * @return A list of PostResponseDTOs representing the user's posts
     */
    public List<PostResponseDTO> getUserPosts(String username) {
        return postRepository.findByAuthorUsernameOrderByCreatedAtDesc(username).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Deletes a post if the current user is the owner of that post.
     *
     * @param postId The ID of the post to delete
     * @param currentUsername The username of the currently logged-in user
     * @throws EntityNotFoundException if the post is not found
     * @throws SecurityException if the current user does not own the post
     */
    public void deletePost(Long postId, String currentUsername) {
        Post post = postRepository.findByIdWithAuthor(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        ownershipValidator.checkOwnership(post, currentUsername);
        postRepository.delete(post);
    }

    private PostResponseDTO toDto(Post post) {
        PostResponseDTO dto = new PostResponseDTO();
        dto.setId(post.getId());
        dto.setContent(post.getContent());
        dto.setImageUrl(post.getImagePath());
        dto.setUsername(post.getAuthor().getUsername());
        dto.setCreatedAt(post.getCreatedAt());
        return dto;
    }
}
