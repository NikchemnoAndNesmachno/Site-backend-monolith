package ua.nin.comments.exception;

import org.junit.jupiter.api.Test;
import ua.nin.comments.exception.exceptions.BadRequestException;
import ua.nin.comments.exception.exceptions.ForbiddenException;
import ua.nin.comments.exception.exceptions.NotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommentExceptionTest {

    @Test
    void exceptions_holdMessages() {
        assertEquals("bad", new BadRequestException("bad").getMessage());
        assertEquals("forbidden", new ForbiddenException("forbidden").getMessage());
        assertEquals("not found", new NotFoundException("not found").getMessage());
    }
}
