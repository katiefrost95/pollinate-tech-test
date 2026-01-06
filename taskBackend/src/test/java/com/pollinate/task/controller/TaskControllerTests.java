package com.pollinate.task.controller;

import com.pollinate.task.model.TaskRequest;
import com.pollinate.task.security.AuthTokenFilter;
import com.pollinate.task.security.JwtUtil;
import com.pollinate.task.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers =TaskController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
public class TaskControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthTokenFilter authTokenFilter;

    @MockitoBean
    private JwtUtil utils;

    @MockitoBean
    private TaskService service;

    private LocalDate tomorrow = LocalDate.now().plusDays(1);

    private String taskTitle = "Task one";

    private String taskJson = String.format("{\"title\": \"%s\", \"dueDate\": \"%s\"}", taskTitle, tomorrow);;

    @BeforeEach
    public void setup() {
        taskJson = String.format("{\"title\": \"Task one\", \"dueDate\": \"%s\"}", tomorrow);
    }

    @Test
    public void getsAllTasks_success() throws Exception {

        List<TaskRequest> tasks = List.of(
                TaskRequest.builder().title("Task A").dueDate(tomorrow).build(),
                TaskRequest.builder().title("Task B").dueDate(null).build()
        );
        when(service.getTasks()).thenReturn(tasks);

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tasks", hasSize(2)))
                .andExpect(jsonPath("$.tasks[0].title").value("Task A"))
                .andExpect(jsonPath("$.tasks[0].dueDate").value(tomorrow.toString()))
                .andExpect(jsonPath("$.tasks[1].title").value("Task B"))
                .andExpect(jsonPath("$.tasks[1].dueDate").doesNotExist()); // null is often omitted or serialized as null depending on ObjectMapper config

        verify(service, times(1)).getTasks();
    }

    @Test
    public void createNewTask_success() throws Exception {

        List<TaskRequest> tasks = List.of(
                TaskRequest.builder().title(taskTitle).dueDate(tomorrow).build()
        );

        when(service.createTask(any(TaskRequest.class))).thenReturn(tasks);

        // When & Then
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tasks", hasSize(1)))
                .andExpect(jsonPath("$.tasks[0].title").value(taskTitle))
                .andExpect(jsonPath("$.tasks[0].dueDate").value(tomorrow.toString()));


        ArgumentCaptor<TaskRequest> captor = ArgumentCaptor.forClass(TaskRequest.class);
        verify(service, times(1)).createTask(captor.capture());
        TaskRequest sent = captor.getValue();
        assert sent.getTitle().equals(taskTitle);
    }

    @Test
    public void updateTask_success() throws Exception {
        Long id = 1L;
        List<TaskRequest> updatedTasks = List.of(
                TaskRequest.builder().title(taskTitle).dueDate(tomorrow).build()
        );

        when(service.updateTasks(any(TaskRequest.class), eq(id))).thenReturn(updatedTasks);


        mockMvc.perform(put("/tasks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(status().isAccepted())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tasks", hasSize(1)))
                .andExpect(jsonPath("$.tasks[0].title").value(taskTitle));

        ArgumentCaptor<TaskRequest> captor = ArgumentCaptor.forClass(TaskRequest.class);
        verify(service, times(1)).updateTasks(captor.capture(), eq(id));
        TaskRequest sent = captor.getValue();
        assert sent.getTitle().equals(taskTitle);
    }

    @Test
    public void updateTask_invalid() throws Exception {
        Long id = 1L;
        String invalidJason = "{}";

        mockMvc.perform(put("/tasks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJason))
                .andExpect(status().isBadRequest());

        verify(service, never()).updateTasks(any(TaskRequest.class), anyLong());
    }

    @Test
    public void deleteTask() throws Exception {
        Long id = 42L;
        when(service.deleteTask(id)).thenReturn(List.of());

        mockMvc.perform(delete("/tasks/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(isEmptyString()));

        verify(service, times(1)).deleteTask(id);
    }

    @Test
    public void givenInvalidPayload_whenCreateTask_thenValidationError() throws Exception {
        String invalidJson = "{}";

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(service, never()).createTask(any(TaskRequest.class));

    }

}
