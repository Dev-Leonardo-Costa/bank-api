package com.leonardo.bank_api.auth.controller;


import com.leonardo.bank_api.auth.controller.authcontrollerdocs.PixControllerDocs;
import com.leonardo.bank_api.auth.service.AuthService;
import com.leonardo.bank_api.security.dto.request.LoginRequest;
import com.leonardo.bank_api.security.dto.response.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController implements PixControllerDocs {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}