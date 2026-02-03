package ua.nin.comments.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ua.nin.comments.dto.CommentResponse;
import ua.nin.comments.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentResponseMapper {
    CommentResponse toDto(Comment comment);

    @Mapping(
            target = "body",
            expression = "java(comment.getStatus() == CommentStatus.ACTIVE ? comment.getBody() : \"[hidden]\")"
    )
    CommentResponse toDtoPublic(Comment comment);
}
