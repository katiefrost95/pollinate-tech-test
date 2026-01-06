
package com.pollinate.task.service;

import com.pollinate.task.model.TaskRequest;
import com.pollinate.task.repository.TaskRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTests {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private final String username = "alice";
    private final LocalDate tomorrow = LocalDate.now().plusDays(1);

    private void authenticateAs(String user) {
        Authentication auth = new UsernamePasswordAuthenticationToken(user, "N/A");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void getTasks_success() {
        authenticateAs(username);

        List<TaskRequest> tasks = List.of(
                TaskRequest.builder().title("Task A").dueDate(tomorrow).owner(username).build()
        );
        when(taskRepository.findByOwner(username)).thenReturn(tasks);

        List<TaskRequest> result = taskService.getTasks();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("Task A");
        assertThat(result.getFirst().getDueDate()).isEqualTo(tomorrow);

        verify(taskRepository, times(1)).findByOwner(username);
    }

    @Test
    public void createTask_success() {
        authenticateAs(username);

        TaskRequest incoming = TaskRequest.builder()
                .title("New Task")
                .dueDate(tomorrow)
                .build();

        List<TaskRequest> returned = List.of(
                TaskRequest.builder().title("New Task").dueDate(tomorrow).owner(username).build()
        );

        // Save then return all for owner
        when(taskRepository.findByOwner(username)).thenReturn(returned);

        List<TaskRequest> result = taskService.createTask(incoming);

        // Returned list assertions
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("New Task");
        assertThat(result.getFirst().getDueDate()).isEqualTo(tomorrow);
        assertThat(result.getFirst().getOwner()).isEqualTo(username);

        // Verify saved entity had owner set by service
        ArgumentCaptor<TaskRequest> saveCaptor = ArgumentCaptor.forClass(TaskRequest.class);
        verify(taskRepository, times(1)).save(saveCaptor.capture());
        TaskRequest saved = saveCaptor.getValue();
        assertThat(saved.getTitle()).isEqualTo("New Task");
        assertThat(saved.getDueDate()).isEqualTo(tomorrow);
        assertThat(saved.getOwner()).isEqualTo(username);

        // Verify final list retrieval
        verify(taskRepository, times(1)).findByOwner(username);
    }

    @Test
    public void updateTasks_success() {
        authenticateAs(username);

        Long id = 10L;

        // Existing entity owned by user
        TaskRequest existing = TaskRequest.builder()
                .title("Old Title")
                .dueDate(LocalDate.now())
                .owner(username)
                .build();

        // Incoming updates
        TaskRequest patch = TaskRequest.builder()
                .title("Updated Title")
                .dueDate(tomorrow)
                .build();

        List<TaskRequest> returned = List.of(
                TaskRequest.builder().title("Updated Title").dueDate(tomorrow).owner(username).build()
        );

        when(taskRepository.findByIdAndOwner(id, username)).thenReturn(Optional.of(existing));
        when(taskRepository.findByOwner(username)).thenReturn(returned);

        List<TaskRequest> result = taskService.updateTasks(patch, id);

        // Returned list assertions
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Updated Title");
        assertThat(result.get(0).getDueDate()).isEqualTo(tomorrow);

        // Verify repository.save called with the mutated existing entity
        ArgumentCaptor<TaskRequest> saveCaptor = ArgumentCaptor.forClass(TaskRequest.class);
        verify(taskRepository, times(1)).save(saveCaptor.capture());
        TaskRequest saved = saveCaptor.getValue();
        assertThat(saved.getTitle()).isEqualTo("Updated Title");
        assertThat(saved.getDueDate()).isEqualTo(tomorrow);
        assertThat(saved.getOwner()).isEqualTo(username);

        // Verify lookups
        verify(taskRepository, times(1)).findByIdAndOwner(id, username);
        verify(taskRepository, times(1)).findByOwner(username);
    }

    @Test
    public void updateTasks_notFound_throws() {
        authenticateAs(username);

        Long id = 99L;
        TaskRequest patch = TaskRequest.builder()
                .title("Patch")
                .dueDate(null)
                .build();

        when(taskRepository.findByIdAndOwner(id, username)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.updateTasks(patch, id));
        assertThat(ex.getMessage()).contains("Task not found or not owned by user");

        verify(taskRepository, times(1)).findByIdAndOwner(id, username);
        verify(taskRepository, never()).save(any(TaskRequest.class));
        verify(taskRepository, never()).findByOwner(anyString());
    }

    @Test
    public void deleteTask_success() {
        authenticateAs(username);

        Long id = 7L;

        TaskRequest existing = TaskRequest.builder()
                .title("To delete")
                .dueDate(null)
                .owner(username)
                .build();

        List<TaskRequest> remaining = List.of(
                TaskRequest.builder().title("Remaining").dueDate(tomorrow).owner(username).build()
        );

        when(taskRepository.findByIdAndOwner(id, username)).thenReturn(Optional.of(existing));
        when(taskRepository.findByOwner(username)).thenReturn(remaining);

        List<TaskRequest> result = taskService.deleteTask(id);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Remaining");
        assertThat(result.get(0).getOwner()).isEqualTo(username);

        // Verify delete and subsequent fetch
        ArgumentCaptor<TaskRequest> deleteCaptor = ArgumentCaptor.forClass(TaskRequest.class);
        verify(taskRepository, times(1)).delete(deleteCaptor.capture());
        TaskRequest deleted = deleteCaptor.getValue();
        assertThat(deleted.getTitle()).isEqualTo("To delete");
        assertThat(deleted.getOwner()).isEqualTo(username);

        verify(taskRepository, times(1)).findByOwner(username);
    }

    @Test
    public void deleteTask_notFound_throws() {
        authenticateAs(username);

        Long id = 123L;

        when(taskRepository.findByIdAndOwner(id, username)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.deleteTask(id));
        assertThat(ex.getMessage()).contains("Task not found or not owned by user");

        verify(taskRepository, times(1)).findByIdAndOwner(id, username);
        verify(taskRepository, never()).delete(any(TaskRequest.class));
        verify(taskRepository, never()).findByOwner(anyString());
    }
}
