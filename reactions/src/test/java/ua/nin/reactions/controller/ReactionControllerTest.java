package ua.nin.reactions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ua.nin.reactions.dto.PutReactionRequest;
import ua.nin.reactions.dto.ReactionActionResponse;
import ua.nin.reactions.service.ReactionService;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReactionControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ReactionService reactionService;

    @InjectMocks
    private ReactionController reactionController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(reactionController).build();
    }

    @Test
    void put_returnsResponse() throws Exception {
        ReactionActionResponse response = new ReactionActionResponse("VIDEO", 2L, "LIKE", Map.of("LIKE", 1L), Instant.now());
        when(reactionService.put(any(Long.class), any(PutReactionRequest.class))).thenReturn(response);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("1");

        PutReactionRequest request = new PutReactionRequest("VIDEO", 2L, "LIKE");

        mockMvc.perform(put("/api/v1/reactions")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myReaction").value("LIKE"));
    }

    @Test
    void counts_returnsMap() throws Exception {
        when(reactionService.counts("VIDEO", 2L)).thenReturn(Map.of("LIKE", 3L));

        mockMvc.perform(get("/api/v1/reactions/VIDEO/2/counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.LIKE").value(3));
    }

    @Test
    void my_returnsValue() throws Exception {
        when(reactionService.myReaction(1L, "VIDEO", 2L)).thenReturn("LIKE");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("1");

        mockMvc.perform(get("/api/v1/reactions/VIDEO/2/my")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string("LIKE"));
    }
}
