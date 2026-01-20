package ua.nin.identity.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Void> reset(@Valid @RequestBody ResetPasswordRequest req) {
        passwordService.reset(req.token(), req.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change")
    public ResponseEntity<Void> change(@Valid @RequestBody ChangePasswordRequest req) {
        passwordService.change(req.currentPassword(), req.newPassword());
        return ResponseEntity.noContent().build();
    }
}
