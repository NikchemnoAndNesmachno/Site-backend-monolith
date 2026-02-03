package ua.nin.reactions.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.nin.reactions.dto.PutReactionRequest;
import ua.nin.reactions.exception.exceptions.UnknownReactionTypeException;
import ua.nin.reactions.model.Reaction;
import ua.nin.reactions.model.ReactionCount;
import ua.nin.reactions.repository.ReactionCountRepository;
import ua.nin.reactions.repository.ReactionRepository;
import ua.nin.reactions.repository.ReactionTypeRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactionServiceTest {

    @Mock
    private ReactionRepository reactionRepository;
    @Mock
    private ReactionTypeRepository reactionTypeRepository;
    @Mock
    private ReactionCountRepository reactionCountRepository;

    @InjectMocks
    private ReactionService reactionService;

    @Test
    void put_unknownReactionType_throws() {
        when(reactionTypeRepository.existsById("LIKE")).thenReturn(false);
        PutReactionRequest request = PutReactionRequest.builder()
                .targetType("VIDEO")
                .targetId(2L)
                .reactionCode("LIKE")
                .build();


        assertThatThrownBy(() -> reactionService.put(1L, request))
                .isInstanceOf(UnknownReactionTypeException.class);
    }

    @Test
    void put_createNewReaction() {
        when(reactionTypeRepository.existsById("LIKE")).thenReturn(true);
        when(reactionRepository.findForUpdate(1L, "VIDEO", 2L)).thenReturn(Optional.empty());
        when(reactionCountRepository.findByTarget("VIDEO", 2L))
                .thenReturn(List.of(new ReactionCount(new ReactionCount.ReactionCountId("VIDEO", 2L, "LIKE"), 1L, Instant.now())));

        var response = reactionService.put(1L, new PutReactionRequest("VIDEO", 2L, "LIKE"));

        assertEquals("LIKE", response.myReaction());
        verify(reactionCountRepository).applyDelta("VIDEO", 2L, "LIKE", 1);
    }

    @Test
    void put_toggleOffReaction() {
        when(reactionTypeRepository.existsById("LIKE")).thenReturn(true);
        Reaction reaction = Reaction.builder().reactionCode("LIKE").targetType("VIDEO").targetId(2L).build();
        when(reactionRepository.findForUpdate(1L, "VIDEO", 2L)).thenReturn(Optional.of(reaction));
        when(reactionCountRepository.findByTarget("VIDEO", 2L))
                .thenReturn(List.of(new ReactionCount(new ReactionCount.ReactionCountId("VIDEO", 2L, "LIKE"), 0L, Instant.now())));

        var response = reactionService.put(1L, new PutReactionRequest("VIDEO", 2L, "LIKE"));

        assertNull(response.myReaction());
        verify(reactionCountRepository).applyDelta("VIDEO", 2L, "LIKE", -1);
        assertNotNull(reaction.getRevokedAt());
    }

    @Test
    void put_changeReaction() {
        when(reactionTypeRepository.existsById("LIKE")).thenReturn(true);
        Reaction reaction = Reaction.builder().reactionCode("WOW").targetType("VIDEO").targetId(2L).build();
        when(reactionRepository.findForUpdate(1L, "VIDEO", 2L)).thenReturn(Optional.of(reaction));
        when(reactionCountRepository.findByTarget("VIDEO", 2L))
                .thenReturn(List.of(new ReactionCount(new ReactionCount.ReactionCountId("VIDEO", 2L, "LIKE"), 1L, Instant.now())));

        var response = reactionService.put(1L, new PutReactionRequest("VIDEO", 2L, "LIKE"));

        assertEquals("LIKE", response.myReaction());
        verify(reactionCountRepository).applyDelta("VIDEO", 2L, "WOW", -1);
        verify(reactionCountRepository).applyDelta("VIDEO", 2L, "LIKE", 1);
    }

    @Test
    void put_activateRevoked() {
        when(reactionTypeRepository.existsById("LIKE")).thenReturn(true);
        Reaction reaction = Reaction.builder().reactionCode("WOW").targetType("VIDEO").targetId(2L).revokedAt(Instant.now()).build();
        when(reactionRepository.findForUpdate(1L, "VIDEO", 2L)).thenReturn(Optional.of(reaction));
        when(reactionCountRepository.findByTarget("VIDEO", 2L))
                .thenReturn(List.of(new ReactionCount(new ReactionCount.ReactionCountId("VIDEO", 2L, "LIKE"), 2L, Instant.now())));

        var response = reactionService.put(1L, new PutReactionRequest("VIDEO", 2L, "LIKE"));

        assertEquals("LIKE", response.myReaction());
        verify(reactionCountRepository).applyDelta("VIDEO", 2L, "LIKE", 1);
        assertNull(reaction.getRevokedAt());
    }

    @Test
    void counts_returnsMap() {
        when(reactionCountRepository.findByTarget("VIDEO", 2L))
                .thenReturn(List.of(new ReactionCount(new ReactionCount.ReactionCountId("VIDEO", 2L, "LIKE"), 3L, Instant.now())));

        Map<String, Long> counts = reactionService.counts("video", 2L);

        assertEquals(3L, counts.get("LIKE"));
    }

    @Test
    void myReaction_returnsNullWhenMissing() {
        when(reactionRepository.findAny(1L, "VIDEO", 2L)).thenReturn(Optional.empty());

        assertNull(reactionService.myReaction(1L, "video", 2L));
    }
}
