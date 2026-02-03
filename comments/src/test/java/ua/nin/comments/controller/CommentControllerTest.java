package ua.nin.comments.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ua.nin.comments.dto.CommentResponse;
import ua.nin.comments.dto.CreateCommentRequest;
import ua.nin.comments.dto.UpdateCommentRequest;
import ua.nin.comments.model.CommentStatus;
import ua.nin.comments.service.CommentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(commentController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void create_returnsResponse() throws Exception {
        CommentResponse response = new CommentResponse(1L, 2L, "VIDEO", 3L, null, "body", CommentStatus.ACTIVE, null, null);
        when(commentService.create(any(Long.class), any(CreateCommentRequest.class))).thenReturn(response);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("2");

        CreateCommentRequest request = new CreateCommentRequest("VIDEO", 3L, null, "body");

        mockMvc.perform(post("/api/v1/comments")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void listRoot_returnsPage() throws Exception {
        Page<CommentResponse> page = new PageImpl<>(
                List.of(new CommentResponse(1L, 2L, "VIDEO", 3L, null, "body", CommentStatus.ACTIVE, null, null)),
                PageRequest.of(0, 5),
                1
        );
        when(commentService.listRoot(any(String.class), any(Long.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/comments")
                        .param("targetType", "VIDEO")
                        .param("targetId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void listReplies_returnsPage() throws Exception {
        Page<CommentResponse> page = new PageImpl<>(List.of(new CommentResponse(1L, 2L, "VIDEO", 3L, null, "body", CommentStatus.ACTIVE, null, null)),
                PageRequest.of(0, 20),
                1
        );
        when(commentService.listReplies(any(Long.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/comments/1/replies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void update_returnsResponse() throws Exception {
        CommentResponse response = new CommentResponse(1L, 2L, "VIDEO", 3L, null, "updated", CommentStatus.ACTIVE, null, null);
        when(commentService.update(any(Long.class), any(Long.class), any(UpdateCommentRequest.class))).thenReturn(response);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("2");

        UpdateCommentRequest request = new UpdateCommentRequest("updated");

        mockMvc.perform(patch("/api/v1/comments/1")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body").value("updated"));
    }

    @Test
    void delete_returnsNoContent() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("2");

        mockMvc.perform(delete("/api/v1/comments/1")
                        .principal(authentication))
                .andExpect(status().isNoContent());
    }
}
