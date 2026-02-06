package ua.nin.comments.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ua.nin.comments.dto.CommentResponse;
import ua.nin.comments.model.Comment;
import ua.nin.comments.model.CommentStatus;

import static org.junit.jupiter.api.Assertions.*;

class CommentResponseMapperTest {

    private final CommentResponseMapper mapper = Mappers.getMapper(CommentResponseMapper.class);

    @Test
    void toDto_mapsBody() {
        Comment comment = Comment.builder()
                .id(1L)
                .authorUserId(2L)
                .targetType("VIDEO")
                .targetId(3L)
                .body("body")
                .status(CommentStatus.ACTIVE)
                .build();

        CommentResponse dto = mapper.toDto(comment);

        assertEquals("body", dto.body());
        assertEquals(1L, dto.id());
    }

    @Test
    void toDtoPublic_hidesBodyWhenNotActive() {
        Comment comment = Comment.builder()
                .id(1L)
                .authorUserId(2L)
                .targetType("VIDEO")
                .targetId(3L)
                .body("body")
                .status(CommentStatus.DELETED)
                .build();

        CommentResponse dto = mapper.toDtoPublic(comment);

        assertEquals("[hidden]", dto.body());
    }
}
