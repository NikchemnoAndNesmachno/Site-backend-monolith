package ua.nin.comments.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ua.nin.comments.dto.CommentResponse;
import ua.nin.comments.dto.CreateCommentRequest;
import ua.nin.comments.dto.UpdateCommentRequest;
import ua.nin.comments.exception.exceptions.BadRequestException;
import ua.nin.comments.exception.exceptions.ForbiddenException;
import ua.nin.comments.exception.exceptions.NotFoundException;
import ua.nin.comments.mapper.CommentResponseMapper;
import ua.nin.comments.model.Comment;
import ua.nin.comments.model.CommentStatus;
import ua.nin.comments.repository.CommentClosureRepository;
import ua.nin.comments.repository.CommentRepository;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentClosureRepository closureRepository;
    @Mock
    private CommentResponseMapper commentResponseMapper;

    @InjectMocks
    private CommentService commentService;

    @Test
    void create_rootComment_insertsSelf() {
        CreateCommentRequest request = new CreateCommentRequest("VIDEO", 10L, null, "hello");
        Comment saved = Comment.builder().id(1L).build();
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);
        when(commentResponseMapper.toDto(saved)).thenReturn(new CommentResponse(1L, 2L, "VIDEO", 10L, null, "hello", CommentStatus.ACTIVE, null, null));

        CommentResponse response = commentService.create(2L, request);

        assertEquals(1L, response.id());
        verify(closureRepository).insertSelf(1L);
    }

    @Test
    void create_replyDifferentTarget_throws() {
        Comment parent = Comment.builder().id(1L).targetType("VIDEO").targetId(9L).build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(parent));

        CreateCommentRequest request = new CreateCommentRequest("POST", 10L, 1L, "hello");

        assertThatThrownBy(() -> commentService.create(2L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("different target");
    }

    @Test
    void create_replyDepthExceeded_throws() {
        ReflectionTestUtils.setField(commentService, "maxDepth", 1);
        Comment parent = Comment.builder().id(1L).targetType("VIDEO").targetId(9L).build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(closureRepository.maxDepthFromAncestors(1L)).thenReturn(1);

        CreateCommentRequest request = new CreateCommentRequest("VIDEO", 9L, 1L, "hello");

        assertThatThrownBy(() -> commentService.create(2L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Max comment depth");
    }

    @Test
    void update_notAuthor_throws() {
        Comment comment = Comment.builder().id(1L).authorUserId(10L).status(CommentStatus.ACTIVE).build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        var request = new UpdateCommentRequest("body");

        assertThatThrownBy(() -> commentService.update(99L, 1L, request))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void update_inactive_throws() {
        Comment comment = Comment.builder().id(1L).authorUserId(10L).status(CommentStatus.DELETED).build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        var request = new UpdateCommentRequest("body");

        assertThatThrownBy(() -> commentService.update(10L, 1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void delete_marksDeleted() {
        Comment comment = Comment.builder().id(1L).authorUserId(10L).status(CommentStatus.ACTIVE).build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.delete(10L, 1L);

        assertEquals(CommentStatus.DELETED, comment.getStatus());
        assertEquals("[deleted]", comment.getBody());
        assertTrue(comment.getDeletedAt().isBefore(Instant.now().plusSeconds(1)));
    }

    @Test
    void delete_notFound_throws() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.delete(1L, 1L))
                .isInstanceOf(NotFoundException.class);
    }
}
