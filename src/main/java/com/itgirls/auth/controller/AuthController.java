package com.itgirls.auth.controller;

import com.itgirls.auth.dto.RegistrationRequestDto;
import com.itgirls.auth.entity.User;
import com.itgirls.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.itgirls.auth.dto.LoginRequestDto;
import com.itgirls.auth.dto.LoginResponseDto;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegistrationRequestDto registrationRequestDto) {
        User user = authService.register(registrationRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/activate")
    public ResponseEntity<User> activate(@RequestParam String token) {
        User user = authService.activateAccount(token);
        return ResponseEntity.ok(user);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login
            (@Valid @RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto loginResponseDto = authService.login(loginRequestDto);
        return ResponseEntity.ok(loginResponseDto);
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.ok().build();
    }
}
