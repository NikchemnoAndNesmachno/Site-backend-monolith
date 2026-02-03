package ua.nin.comments.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ua.nin.comments.dto.CommentResponse;
import ua.nin.comments.model.Comment;
import ua.nin.comments.model.CommentStatus;

@Mapper(componentModel = "spring")
public interface CommentResponseMapper {
    CommentResponse toDto(Comment comment);

    @Mapping(target = "body", qualifiedByName = "mapPublicBody")
    CommentResponse toDtoPublic(Comment comment);

    @Named("mapPublicBody")
    default String mapPublicBody(Comment comment) {
        return (comment.getStatus() == CommentStatus.ACTIVE) ? comment.getBody() : "[hidden]";
    }
}
