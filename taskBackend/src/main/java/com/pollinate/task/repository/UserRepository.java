package com.pollinate.task.repository;

import com.pollinate.task.model.AuthRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<AuthRequest, Long> {

    AuthRequest findByUsername(String username);

    boolean existsByUsername(String username);
}