package ua.nin.identity.profile.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ua.nin.identity.profile.dto.PublicProfileResponse;
import ua.nin.identity.profile.model.Privacy;
import ua.nin.identity.profile.model.Profile;

import static org.junit.jupiter.api.Assertions.*;

class PublicProfileResponseMapperTest {

    private final PublicProfileResponseMapper mapper = Mappers.getMapper(PublicProfileResponseMapper.class);

    @Test
    void mapsPublicProfile() {
        Profile profile = Profile.builder()
                .userId(12L)
                .username("public-user")
                .displayName("Public")
                .privacy(Privacy.PUBLIC)
                .build();

        PublicProfileResponse dto = mapper.toDto(profile);

        assertEquals("public-user", dto.username());
        assertEquals("Public", dto.displayName());
    }
}