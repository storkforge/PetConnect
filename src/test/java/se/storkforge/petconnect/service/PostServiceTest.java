package se.storkforge.petconnect.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PostServiceTest {

    private PostService postService;
    private PostRepository postRepository;
    private FileStorageService fileStorageService;
    private OwnershipValidator ownershipValidator;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        postRepository = mock(PostRepository.class);
        fileStorageService = mock(FileStorageService.class);
        ownershipValidator = mock(OwnershipValidator.class);
        userRepository = mock(UserRepository.class);
        postService = new PostService(postRepository, fileStorageService, ownershipValidator, userRepository);
    }

    @Test
    void createPost_shouldCreateAndReturnDto() {
        PostInputDTO dto = new PostInputDTO();
        dto.setContent("Hello world");

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileStorageService.store(mockFile, "posts")).thenReturn("/path/to/file.jpg");

        User user = new User();
        user.setUsername("testuser");
        user.setId(1L);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);

        PostResponseDTO response = postService.createPost(dto, mockFile, "testuser");

        verify(postRepository).save(captor.capture());
        Post savedPost = captor.getValue();

        assertEquals("Hello world", savedPost.getContent());
        assertEquals("/path/to/file.jpg", savedPost.getImagePath());
        assertEquals("testuser", response.getUsername());
    }

    @Test
    void getUserPosts_shouldReturnListOfDtos() {
        User user = new User();
        user.setUsername("testuser");

        Post post = new Post();
        post.setId(1L);
        post.setContent("Test content");
        post.setAuthor(user);

        when(postRepository.findByAuthorUsernameOrderByCreatedAtDesc("testuser"))
                .thenReturn(List.of(post));

        List<PostResponseDTO> dtos = postService.getUserPosts("testuser");

        assertEquals(1, dtos.size());
        assertEquals("Test content", dtos.get(0).getContent());
    }

    @Test
    void deletePost_shouldCallRepositoryDelete() {
        User user = new User();
        user.setUsername("testuser");
        Post post = new Post();
        post.setId(1L);
        post.setAuthor(user);

        when(postRepository.findByIdWithAuthor(1L)).thenReturn(Optional.of(post));

        postService.deletePost(1L, "testuser");

        verify(ownershipValidator).checkOwnership(post, "testuser");
        verify(postRepository).delete(post);
    }

    @Test
    void createPost_shouldThrowIfUserNotFound() {
        PostInputDTO dto = new PostInputDTO();
        dto.setContent("Hello");
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            postService.createPost(dto, null, "ghost");
        });
    }

    @Test
    void getAllPosts_shouldMapToDtos() {
        User user = new User();
        user.setUsername("user");

        Post post = new Post();
        post.setId(1L);
        post.setContent("Some post");
        post.setAuthor(user);

        when(postRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(post)));

        List<PostResponseDTO> dtos = postService.getAllPosts(PageRequest.of(0, 10));

        assertEquals(1, dtos.size());
        assertEquals("Some post", dtos.get(0).getContent());
    }

}
