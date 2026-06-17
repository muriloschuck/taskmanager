package com.unisinos.taskmanager.controller;

import com.unisinos.taskmanager.dto.AuthResponseDTO;
import com.unisinos.taskmanager.dto.LoginRequestDTO;
import com.unisinos.taskmanager.model.BlacklistedToken;
import com.unisinos.taskmanager.repository.BlacklistedTokenRepository;
import com.unisinos.taskmanager.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String jwtToken = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(new AuthResponseDTO(jwtToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }
        String jwt = authHeader.substring(7);
        // Store the raw token in blacklist
        if (!blacklistedTokenRepository.existsByToken(jwt)) {
            BlacklistedToken blacklisted = BlacklistedToken.builder().token(jwt).build();
            blacklistedTokenRepository.save(blacklisted);
        }
        return ResponseEntity.ok().build();
    }
}