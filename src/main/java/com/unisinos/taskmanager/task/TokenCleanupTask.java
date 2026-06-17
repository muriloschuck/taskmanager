package com.unisinos.taskmanager.task;

import com.unisinos.taskmanager.model.BlacklistedToken;
import com.unisinos.taskmanager.repository.BlacklistedTokenRepository;
import com.unisinos.taskmanager.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupTask {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final JwtService jwtService;

    // Run every hour
    @Scheduled(fixedDelay = 3600000)
    public void cleanupExpiredTokens() {
        log.info("Token cleanup started");
        List<BlacklistedToken> tokens = blacklistedTokenRepository.findAll();
        int removed = 0;
        for (BlacklistedToken t : tokens) {
            try {
                if (jwtService.isTokenExpiredPublic(t.getToken())) {
                    blacklistedTokenRepository.delete(t);
                    removed++;
                }
            } catch (Exception e) {
                log.warn("Token cleanup: failed to parse token, removing it", e);
                blacklistedTokenRepository.delete(t);
                removed++;
            }
        }
        log.info("Token cleanup completed: {} expired tokens removed out of {} total", removed, tokens.size());
    }
}
