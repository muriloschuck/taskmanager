package com.unisinos.taskmanager.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY",
                "testSecretKeyThatIsLongEnoughForHS256Algorithm1234567890");
    }

    private UserDetails buildUserDetails(String email) {
        return new User(email, "password", Collections.emptyList());
    }

    @Test
    void generateToken_returnsNonNullToken() {
        UserDetails userDetails = buildUserDetails("test@test.com");

        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotNull().isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    void extractUsername_returnsCorrectEmail() {
        UserDetails userDetails = buildUserDetails("user@example.com");
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("user@example.com");
    }

    @Test
    void isTokenValid_withValidToken_returnsTrue() {
        UserDetails userDetails = buildUserDetails("valid@test.com");
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_withDifferentUsername_returnsFalse() {
        UserDetails originalUser = buildUserDetails("original@test.com");
        UserDetails differentUser = buildUserDetails("different@test.com");
        String token = jwtService.generateToken(originalUser);

        boolean isValid = jwtService.isTokenValid(token, differentUser);

        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenExpiredPublic_withMalformedToken_returnsTrue() {
        boolean result = jwtService.isTokenExpiredPublic("this.is.not.a.valid.jwt");

        assertThat(result).isTrue();
    }
}
