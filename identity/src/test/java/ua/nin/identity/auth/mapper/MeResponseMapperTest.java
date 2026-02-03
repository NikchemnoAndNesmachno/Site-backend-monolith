package ua.nin.identity.auth.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ua.nin.identity.auth.dto.MeResponse;
import ua.nin.identity.auth.model.Role;
import ua.nin.identity.auth.model.Status;
import ua.nin.identity.auth.model.User;

import static org.junit.jupiter.api.Assertions.*;

class MeResponseMapperTest {

    private final MeResponseMapper mapper = Mappers.getMapper(MeResponseMapper.class);

    @Test
    void mapsUserToDto() {
        User user = User.builder()
                .id(5L)
                .email("user@site.com")
                .status(Status.ACTIVE)
                .role(Role.USER)
                .build();

        MeResponse dto = mapper.toDto(user);

        assertEquals(5L, dto.userId());
        assertEquals("user@site.com", dto.email());
    }
}