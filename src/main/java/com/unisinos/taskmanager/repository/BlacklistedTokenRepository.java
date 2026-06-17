package com.unisinos.taskmanager.repository;

import com.unisinos.taskmanager.model.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, UUID> {
    Optional<BlacklistedToken> findByToken(String token);
    boolean existsByToken(String token);
}

