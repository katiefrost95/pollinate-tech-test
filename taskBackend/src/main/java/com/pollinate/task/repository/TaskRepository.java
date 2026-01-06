package com.pollinate.task.repository;

import com.pollinate.task.model.TaskRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<TaskRequest, Long> {

    List<TaskRequest> findByOwner(String owner);

    Optional<TaskRequest> findByIdAndOwner(Long id, String owner);
}
