package ua.nin.reactions.exception;

import org.junit.jupiter.api.Test;
import ua.nin.reactions.exception.exceptions.UnknownReactionTypeException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReactionExceptionTest {

    @Test
    void exception_holdsMessage() {
        assertEquals("unknown", new UnknownReactionTypeException("unknown").getMessage());
    }
}
