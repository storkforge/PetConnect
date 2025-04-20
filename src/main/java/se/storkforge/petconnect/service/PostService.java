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

    public List<PostResponseDTO> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(this::toDto)
                .toList();
    }

    public List<PostResponseDTO> getUserPosts(String username) {
        return postRepository.findByAuthorUsernameOrderByCreatedAtDesc(username).stream()
                .map(this::toDto)
                .toList();
    }

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
