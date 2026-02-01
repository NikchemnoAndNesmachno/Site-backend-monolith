package ua.nin.reactions.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nin.reactions.dto.ReactionActionResponse;
import ua.nin.reactions.dto.PutReactionRequest;
import ua.nin.reactions.exception.exceptions.UnknownReactionTypeException;
import ua.nin.reactions.model.Reaction;
import ua.nin.reactions.model.ReactionCount;
import ua.nin.reactions.repository.ReactionCountRepository;
import ua.nin.reactions.repository.ReactionRepository;
import ua.nin.reactions.repository.ReactionTypeRepository;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import static ua.nin.common.util.StringHelperUtils.normalizeReactionCode;
import static ua.nin.common.util.StringHelperUtils.normalizeTargetType;


/*
Семантика PUT/reactions:
- якщо реакції не було → створити активну → +1 у counts
- якщо така ж активна була → зняти (revoked_at=now) → -1
- якщо активна була інша → змінити код → -1 old та +1 new
- якщо було знято (revoked) → активувати → +1
 */
@Service
@RequiredArgsConstructor
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final ReactionTypeRepository reactionTypeRepository;
    private final ReactionCountRepository reactionCountRepository;

    @Transactional
    public ReactionActionResponse put(long userId, PutReactionRequest req) {
        String targetType = normalizeTargetType(req.targetType());
        long targetId = req.targetId();
        String newCode = normalizeReactionCode(req.reactionCode());

        if (!reactionTypeRepository.existsById(newCode)) {
            throw new UnknownReactionTypeException("Unknown reaction type: " + newCode);
        }

        Instant now = Instant.now();

        Reaction r = reactionRepository.findForUpdate(userId, targetType, targetId).orElse(null);

        String myReactionAfter;

        if (r == null) {
            createNew(userId, targetType, targetId, newCode);
            myReactionAfter = newCode;

        } else if (r.isActive()) {
            String oldCode = r.getReactionCode();

            if (oldCode.equals(newCode)) {
                // toggle off
                toggleOff(r, now, targetType, targetId, oldCode);
                myReactionAfter = null;

            } else {
                // change reaction
                changeReaction(r, newCode, targetType, targetId, oldCode);
                myReactionAfter = newCode;
            }

        } else {
            // was revoked -> activate (and maybe change type)
            activateRevoked(r, newCode, targetType, targetId);
            myReactionAfter = newCode;
        }

        Map<String, Long> counts = reactionCountRepository.findByTarget(targetType, targetId).stream()
                .collect(Collectors.toMap(rc -> rc.getId().getReactionCode(), ReactionCount::getCount));

        return new ReactionActionResponse(targetType, targetId, myReactionAfter, counts, now);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> counts(String targetTypeRaw, long targetId) {
        String targetType = normalizeTargetType(targetTypeRaw);
        return reactionCountRepository.findByTarget(targetType, targetId).stream()
                .collect(Collectors.toMap(rc -> rc.getId().getReactionCode(), ReactionCount::getCount));
    }

    @Transactional(readOnly = true)
    public String myReaction(long userId, String targetTypeRaw, long targetId) {
        String targetType = normalizeTargetType(targetTypeRaw);
        return reactionRepository.findAny(userId, targetType, targetId)
                .filter(Reaction::isActive)
                .map(Reaction::getReactionCode)
                .orElse(null);
    }

    private void createNew(long userId, String targetType, long targetId, String newCode) {
        // TODO add one-time validation request to media service/module if (targetType.equals("VIDEO")) to check that video exists
        Reaction created = Reaction.builder()
                .userId(userId)
                .targetType(targetType)
                .targetId(targetId)
                .reactionCode(newCode)
                .revokedAt(null)
                .build();
        reactionRepository.save(created);

        reactionCountRepository.applyDelta(targetType, targetId, newCode, +1);
    }

    private void toggleOff(Reaction r, Instant now, String targetType, long targetId, String oldCode){
        // toggle off
        r.setRevokedAt(now);
        reactionRepository.save(r);

        reactionCountRepository.applyDelta(targetType, targetId, oldCode, -1);
    }

    private void changeReaction(Reaction r, String newCode, String targetType, long targetId, String oldCode) {
        // change reaction
        r.setReactionCode(newCode);
        reactionRepository.save(r);

        reactionCountRepository.applyDelta(targetType, targetId, oldCode, -1);
        reactionCountRepository.applyDelta(targetType, targetId, newCode, +1);
    }

    private void activateRevoked(Reaction r, String newCode, String targetType, long targetId) {
        // was revoked -> activate (and maybe change type)
        r.setReactionCode(newCode);
        r.setRevokedAt(null);
        reactionRepository.save(r);

        // oldCode був уже неактивний, тому -1 не робимо
        reactionCountRepository.applyDelta(targetType, targetId, newCode, +1);
    }
}
