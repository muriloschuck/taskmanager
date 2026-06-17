package com.unisinos.taskmanager.service;

import com.unisinos.taskmanager.dto.UserRegisterDTO;
import com.unisinos.taskmanager.dto.UserUpdateDTO;
import com.unisinos.taskmanager.exception.DuplicateEmailException;
import com.unisinos.taskmanager.exception.UserNotFoundException;
import com.unisinos.taskmanager.model.User;
import com.unisinos.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User buildUser(UUID id, String email, String name, boolean deleted) {
        return User.builder()
                .id(id)
                .email(email)
                .name(name)
                .passwordHash("hashed-password")
                .deleted(deleted)
                .build();
    }

    // --- createUser ---

    @Test
    void createUser_withValidData_returnsCreatedUser() {
        UserRegisterDTO dto = UserRegisterDTO.builder()
                .name("João")
                .email("joao@test.com")
                .password("senha123")
                .build();

        UUID generatedId = UUID.randomUUID();
        User savedUser = buildUser(generatedId, "joao@test.com", "João", false);

        when(userRepository.findByEmailAndDeletedFalse("joao@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.createUser(dto);

        assertThat(result.getId()).isEqualTo(generatedId);
        assertThat(result.getEmail()).isEqualTo("joao@test.com");
        assertThat(result.getName()).isEqualTo("João");
        verify(passwordEncoder).encode("senha123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_withDuplicateEmail_throwsDuplicateEmailException() {
        UserRegisterDTO dto = UserRegisterDTO.builder()
                .name("João")
                .email("existing@test.com")
                .password("senha123")
                .build();

        User existingUser = buildUser(UUID.randomUUID(), "existing@test.com", "Existing", false);
        when(userRepository.findByEmailAndDeletedFalse("existing@test.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("Email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_encodesPassword_neverSavesPlainText() {
        UserRegisterDTO dto = UserRegisterDTO.builder()
                .name("Test")
                .email("test@test.com")
                .password("plaintext-password")
                .build();

        when(userRepository.findByEmailAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plaintext-password")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createUser(dto);

        assertThat(result.getPasswordHash()).isEqualTo("$2a$10$encoded");
        assertThat(result.getPasswordHash()).isNotEqualTo("plaintext-password");
        verify(passwordEncoder).encode("plaintext-password");
    }

    // --- getUserById ---

    @Test
    void getUserById_whenExists_returnsUser() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, "test@test.com", "Test", false);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        User result = userService.getUserById(id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void getUserById_whenNotExists_throwsUserNotFoundException() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void getUserById_whenUserIsDeleted_throwsUserNotFoundException() {
        UUID id = UUID.randomUUID();
        User deletedUser = buildUser(id, "deleted@test.com", "Deleted", true);

        when(userRepository.findById(id)).thenReturn(Optional.of(deletedUser));

        assertThatThrownBy(() -> userService.getUserById(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
    }

    // --- getUserByEmail ---

    @Test
    void getUserByEmail_whenExists_returnsUser() {
        User user = buildUser(UUID.randomUUID(), "found@test.com", "Found", false);
        when(userRepository.findByEmailAndDeletedFalse("found@test.com")).thenReturn(Optional.of(user));

        User result = userService.getUserByEmail("found@test.com");

        assertThat(result.getEmail()).isEqualTo("found@test.com");
    }

    @Test
    void getUserByEmail_whenNotExists_throwsUserNotFoundException() {
        when(userRepository.findByEmailAndDeletedFalse("nonexistent@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail("nonexistent@test.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
    }

    // --- updateUser ---

    @Test
    void updateUser_withValidName_updatesAndReturns() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, "test@test.com", "Old Name", false);
        UserUpdateDTO dto = UserUpdateDTO.builder().name("New Name").build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(id, dto);

        assertThat(result.getName()).isEqualTo("New Name");
        verify(userRepository).save(user);
    }

    // --- deleteUser ---

    @Test
    void deleteUser_setsDeletedTrue() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, "test@test.com", "Test", false);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.deleteUser(id);

        assertThat(user.isDeleted()).isTrue();
        verify(userRepository).save(user);
    }
}
