package ua.nin.comments.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nin.comments.dto.*;
import ua.nin.comments.exception.exceptions.BadRequestException;
import ua.nin.comments.exception.exceptions.ForbiddenException;
import ua.nin.comments.exception.exceptions.NotFoundException;
import ua.nin.comments.mapper.CommentResponseMapper;
import ua.nin.comments.model.Comment;
import ua.nin.comments.model.CommentStatus;
import ua.nin.comments.repository.CommentClosureRepository;
import ua.nin.comments.repository.CommentRepository;
import ua.nin.comments.repository.projection.VideoCommentCountRow;
import ua.nin.contract.feed.CommentStatsPort;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ua.nin.common.util.StringHelperUtils.normalizeBody;
import static ua.nin.common.util.StringHelperUtils.normalizeTargetType;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService implements CommentStatsPort {

    @Value("${comments.max-depth:20}")
    private int maxDepth;

    private final CommentRepository commentRepository;
    private final CommentClosureRepository closureRepository;
    private final CommentResponseMapper commentResponseMapper;

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> getCommentCountsByVideoIds(Collection<Long> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) {
            return Map.of();
        }

        return commentRepository.findCommentCountsByVideoIds(videoIds).stream()
                .collect(Collectors.toMap(
                        VideoCommentCountRow::getVideoId,
                        VideoCommentCountRow::getCnt
                ));
    }

    @Transactional
    public CommentResponse create(long authorUserId, CreateCommentRequest req) {
        log.debug("Create comment authorUserId={}, targetType={}, targetId={}", authorUserId, req.targetType(), req.targetId());
        String targetType = normalizeTargetType(req.targetType());
        Long targetId = req.targetId();
        Long parentId = req.parentId();

        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new NotFoundException("Parent comment not found"));

            // батьківський коментар повинен бути в тому ж таргеті
            if (!Objects.equals(parent.getTargetId(), targetId) || !Objects.equals(parent.getTargetType(), targetType)) {
                throw new BadRequestException("Parent comment belongs to different target");
            }

            // глибина
            int depth = closureRepository.maxDepthFromAncestors(parentId);
            if (depth + 1 > maxDepth) {
                throw new BadRequestException("Max comment depth reached");
            }
        }

        Comment c = Comment.builder()
                .authorUserId(authorUserId)
                .targetType(targetType)
                .targetId(targetId)
                .parentId(parentId)
                .body(normalizeBody(req.body()))
                .status(CommentStatus.ACTIVE)
                .build();

        c = commentRepository.save(c);

        // closure insert
        if (parentId == null) {
            closureRepository.insertSelf(c.getId());
        } else {
            closureRepository.insertForReply(parentId, c.getId());
        }

        return commentResponseMapper.toDto(c);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> listRoot(String targetType, long targetId, Pageable pageable) {
        log.debug("List root comments targetType={}, targetId={}", targetType, targetId);
        return commentRepository
                .findByTargetTypeAndTargetIdAndParentIdIsNullOrderByCreatedAtDesc(normalizeTargetType(targetType), targetId, pageable)
                .map(commentResponseMapper::toDtoPublic);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> listReplies(long parentId, Pageable pageable) {
        log.debug("List replies parentId={}", parentId);
        return commentRepository
                .findByParentIdOrderByCreatedAtAsc(parentId, pageable)
                .map(commentResponseMapper::toDtoPublic);
    }

    @Transactional
    public CommentResponse update(long userId, long commentId, UpdateCommentRequest req) {
        log.debug("Update comment userId={}, commentId={}", userId, commentId);
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (c.getAuthorUserId() != userId) {
            throw new ForbiddenException("Not your comment");
        }
        if (c.getStatus() != CommentStatus.ACTIVE) {
            throw new BadRequestException("Comment is not active, thus is not editable");
        }

        c.setBody(normalizeBody(req.body()));
        return commentResponseMapper.toDto(c);
    }

    @Transactional
    public void delete(long userId, long commentId) {
        log.debug("Delete comment userId={}, commentId={}", userId, commentId);
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (c.getAuthorUserId() != userId) {
            throw new ForbiddenException("You are not allowed to delete this comment");
        }

        if (c.getStatus() == CommentStatus.DELETED) return;

        c.setStatus(CommentStatus.DELETED);
        c.setDeletedAt(Instant.now());
        c.setBody("[deleted]");
    }
}
