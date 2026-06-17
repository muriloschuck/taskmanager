package com.unisinos.taskmanager.task;

import com.unisinos.taskmanager.model.BlacklistedToken;
import com.unisinos.taskmanager.repository.BlacklistedTokenRepository;
import com.unisinos.taskmanager.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenCleanupTask {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final JwtService jwtService;

    // Run every hour
    @Scheduled(fixedDelay = 3600000)
    public void cleanupExpiredTokens() {
        List<BlacklistedToken> tokens = blacklistedTokenRepository.findAll();
        for (BlacklistedToken t : tokens) {
            try {
                if (jwtService.isTokenExpiredPublic(t.getToken())) {
                    blacklistedTokenRepository.delete(t);
                }
            } catch (Exception e) {
                // In case of parse errors, delete the token to keep DB clean
                blacklistedTokenRepository.delete(t);
            }
        }
    }
}

