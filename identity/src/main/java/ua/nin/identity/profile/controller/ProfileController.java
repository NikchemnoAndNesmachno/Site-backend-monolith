package ua.nin.identity.profile.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ua.nin.identity.profile.dto.*;
import ua.nin.identity.profile.service.ProfileService;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> me(Authentication authentication) {
        long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(profileService.getMyProfile(userId));
    }

    @PatchMapping("/me")
    public ResponseEntity<ProfileResponse> update(Authentication authentication, @Valid @RequestBody UpdateProfileRequest req) {
        long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(profileService.updateMyProfile(userId, req));
    }

    @GetMapping("/by-username/{username}")
    public ResponseEntity<PublicProfileResponse> publicByUsername(@PathVariable String username) {
        return ResponseEntity.ok(profileService.getPublicByUsername(username));
    }
}
