package com.pollinate.task.service;

import com.pollinate.task.model.TaskRequest;
import com.pollinate.task.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth == null) ? null : auth.getName();
    }

    public List<TaskRequest> getTasks() {
        String user = currentUsername();
        log.info("Retrieving tasks for user {}", user);
        return taskRepository.findByOwner(user);
    }

    @Transactional
    public List<TaskRequest> createTask(TaskRequest task) {
        String user = currentUsername();
        log.info("Creating new task for user {} with title: {}", user, task.getTitle());
        task.setOwner(user);
        taskRepository.save(task);
        return taskRepository.findByOwner(user);
    }

    @Transactional
    public List<TaskRequest> updateTasks(TaskRequest task, Long id) {
        String user = currentUsername();
        log.info("Updating task id={} for user {}", id, user);
        TaskRequest existing = taskRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Task not found or not owned by user"));
        // update allowed fields
        existing.setTitle(task.getTitle());
        existing.setDueDate(task.getDueDate());
        taskRepository.save(existing);
        return taskRepository.findByOwner(user);
    }

    @Transactional
    public List<TaskRequest> deleteTask(Long id) {
        String user = currentUsername();
        log.info("Deleting task id={} for user {}", id, user);
        TaskRequest existing = taskRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Task not found or not owned by user"));
        taskRepository.delete(existing);
        return taskRepository.findByOwner(user);
    }
}
