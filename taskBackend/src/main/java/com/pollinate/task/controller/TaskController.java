package com.pollinate.task.controller;

import com.pollinate.task.model.TaskRequest;
import com.pollinate.task.model.TaskResponse;
import com.pollinate.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    TaskService service;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest task) {
        List<TaskRequest> tasks = service.createTask(task);
        TaskResponse response = TaskResponse.builder().tasks(tasks).build();
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<TaskResponse> getTasks() {
        List<TaskRequest> tasks = service.getTasks();
        TaskResponse response = TaskResponse.builder().tasks(tasks).build();
        return ResponseEntity.status(200).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable("id") Long id,
            @Valid @RequestBody TaskRequest task) {
        List<TaskRequest> tasks = service.updateTasks(task, id);
        TaskResponse response = TaskResponse.builder().tasks(tasks).build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<TaskResponse> deleteTask(
            @PathVariable("id") Long id) {
        List<TaskRequest> tasks = service.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

}