package se.storkforge.petconnect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.web.multipart.MultipartFile;
import se.storkforge.petconnect.dto.PostInputDTO;
import se.storkforge.petconnect.dto.PostResponseDTO;
import se.storkforge.petconnect.service.PostService;

class PostControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private PostResponseDTO testPost;
    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(new PostController(postService))
                .setControllerAdvice(new ExceptionHandlerController()).build();

        testPost = new PostResponseDTO();
        testPost.setId(1L);
        testPost.setContent("This is a test post");
        testPost.setImageUrl("http://example.com/image.jpg");
        testPost.setUsername("testuser");
        testPost.setCreatedAt(LocalDateTime.now());

        mockPrincipal = () -> "testuser";
    }

    @Test
    void testGetAllPosts() throws Exception {
        List<PostResponseDTO> posts = Arrays.asList(testPost);
        Pageable pageable = PageRequest.of(0, 10);

        when(postService.getAllPosts(any(Pageable.class))).thenReturn(posts);

        mockMvc.perform(get("/api/posts/all")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].content", is("This is a test post")))
                .andExpect(jsonPath("$[0].imageUrl", is("http://example.com/image.jpg")))
                .andExpect(jsonPath("$[0].username", is("testuser")))
        ;
    }


    @Test
    void testGetUserPosts() throws Exception {
        List<PostResponseDTO> posts = Arrays.asList(testPost);

        when(postService.getUserPosts("testuser")).thenReturn(posts);

        mockMvc.perform(get("/api/posts/user/testuser")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].content", is("This is a test post")))
                .andExpect(jsonPath("$[0].imageUrl", is("http://example.com/image.jpg")))
                .andExpect(jsonPath("$[0].username", is("testuser")));
    }

    @Test
    @WithMockUser(roles = {"USER", "PREMIUM"})
    void testCreatePost() throws Exception {
        PostInputDTO dto = new PostInputDTO();
        dto.setContent("This is a test post");

        MultipartFile file = new MockMultipartFile("file", "filename.txt",
                "text/plain", "some content".getBytes());

        MultipartFile json = new MockMultipartFile("data", "",
                "application/json", objectMapper.writeValueAsString(dto).getBytes());

        when(postService.createPost(any(PostInputDTO.class), any(MultipartFile.class),
                eq("testuser"))).thenReturn(testPost);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/posts")
                        .file((MockMultipartFile) json)
                        .file((MockMultipartFile) file)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON)
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("This is a test post")))
                .andExpect(jsonPath("$.imageUrl", is("http://example.com/image.jpg")))
                .andExpect(jsonPath("$.username", is("testuser")));
    }

    @Test
    void testDeletePost() throws Exception {
        Long postId = 1L;

        doNothing().when(postService).deletePost(postId, "testuser");

        mockMvc.perform(delete("/api/posts/1", postId)
                        .principal(mockPrincipal))
                .andExpect(status().isOk());
    }
}
