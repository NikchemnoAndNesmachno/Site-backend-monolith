package ua.nin.identity.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.nin.identity.auth.dto.ResendVerifyRequest;
import ua.nin.identity.auth.dto.VerifyEmailRequest;
import ua.nin.identity.auth.service.EmailVerificationService;

@RestController
@RequestMapping("/api/v1/auth/email")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/verify")
    public ResponseEntity<Void> verify(@Valid @RequestBody VerifyEmailRequest req) {
        emailVerificationService.verify(req.token());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resend")
    public ResponseEntity<Void> resend(@Valid @RequestBody ResendVerifyRequest req) {
        // важливо: не палимо чи існує email
        emailVerificationService.resend(req.email());
        return ResponseEntity.noContent().build();
    }
}