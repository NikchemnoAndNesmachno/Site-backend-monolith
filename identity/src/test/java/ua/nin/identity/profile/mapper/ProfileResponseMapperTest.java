package ua.nin.identity.profile.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ua.nin.identity.profile.dto.ProfileResponse;
import ua.nin.identity.profile.model.Privacy;
import ua.nin.identity.profile.model.Profile;

import static org.junit.jupiter.api.Assertions.*;

class ProfileResponseMapperTest {

    private final ProfileResponseMapper mapper = Mappers.getMapper(ProfileResponseMapper.class);

    @Test
    void mapsProfileToDto() {
        Profile profile = Profile.builder()
                .userId(10L)
                .username("user")
                .displayName("User")
                .privacy(Privacy.PUBLIC)
                .bio("bio")
                .build();

        ProfileResponse dto = mapper.toDto(profile);

        assertEquals("user", dto.username());
        assertEquals("User", dto.displayName());
    }
}
