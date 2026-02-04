package ua.nin.identity.profile.mapper;

import org.mapstruct.Mapper;
import ua.nin.identity.profile.dto.ProfileResponse;
import ua.nin.identity.profile.model.Profile;

@Mapper(componentModel = "spring")
public interface ProfileResponseMapper {
    ProfileResponse toDto(Profile profile);
}
