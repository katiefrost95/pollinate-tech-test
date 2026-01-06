
package com.pollinate.task.service;

import com.pollinate.task.model.AuthRequest;
import com.pollinate.task.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private final String username = "alice";
    private final String rawPassword = "secret";
    private final String encodedPassword = "ENC(secret)";

    @Test
    public void saveUser_success() {
        AuthRequest incoming = AuthRequest.builder()
                .username(username)
                .password(rawPassword)
                .build();

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        AuthRequest persisted = AuthRequest.builder()
                .username(username)
                .password(encodedPassword)
                .build();
        when(userRepository.save(any(AuthRequest.class))).thenReturn(persisted);

        AuthRequest result = authService.saveUser(incoming);

        // Return value
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getPassword()).isEqualTo(encodedPassword);

        // Interactions and captured arguments
        verify(passwordEncoder, times(1)).encode(rawPassword);

        ArgumentCaptor<AuthRequest> captor = ArgumentCaptor.forClass(AuthRequest.class);
        verify(userRepository, times(1)).save(captor.capture());
        AuthRequest savedArg = captor.getValue();
        assertThat(savedArg.getUsername()).isEqualTo(username);
        assertThat(savedArg.getPassword()).isEqualTo(encodedPassword);
    }

    @Test
    public void saveUser_encoderThrows() {
        AuthRequest incoming = AuthRequest.builder()
                .username("bob")
                .password("pwd")
                .build();

        when(passwordEncoder.encode("pwd")).thenThrow(new IllegalStateException("encoder down"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> authService.saveUser(incoming));
        assertThat(ex).hasMessageContaining("encoder down");

        verify(userRepository, never()).save(any(AuthRequest.class));
    }

    @Test
    public void existsByUsername_trueFalse() {
        when(userRepository.existsByUsername("katie")).thenReturn(true);
        when(userRepository.existsByUsername("charlie")).thenReturn(false);

        Boolean katieExists = authService.existsByUsername("katie");
        Boolean charlieExists = authService.existsByUsername("charlie");

        assertThat(katieExists).isTrue();
        assertThat(charlieExists).isFalse();

        verify(userRepository, times(1)).existsByUsername("katie");
        verify(userRepository, times(1)).existsByUsername("charlie");
    }
}
