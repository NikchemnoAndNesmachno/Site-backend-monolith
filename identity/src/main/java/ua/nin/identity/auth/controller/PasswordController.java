package ua.nin.identity.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ua.nin.identity.auth.dto.ChangePasswordRequest;
import ua.nin.identity.auth.dto.ForgotPasswordRequest;
import ua.nin.identity.auth.dto.ResetPasswordRequest;
import ua.nin.identity.auth.service.PasswordService;

@RestController
@RequestMapping("/api/v1/auth/password")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    @PostMapping("/forgot")
    public ResponseEntity<Void> forgot(@Valid @RequestBody ForgotPasswordRequest req) {
        // завжди 204, навіть якщо email не існує
        passwordService.forgot(req.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> reset(@Valid @RequestParam(name = "token") String resetPasswordToken,
                                      @Valid @RequestBody ResetPasswordRequest req) {
        passwordService.reset(resetPasswordToken, req.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change")
    public ResponseEntity<Void> change(Authentication authentication,
                                       @Valid @RequestBody ChangePasswordRequest req) {
        long userId = Long.parseLong(authentication.getName());
        passwordService.change(userId, req.currentPassword(), req.newPassword());
        return ResponseEntity.noContent().build();
    }
}
