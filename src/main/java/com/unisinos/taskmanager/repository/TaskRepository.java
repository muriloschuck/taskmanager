package com.unisinos.taskmanager.repository;

import com.unisinos.taskmanager.model.Task;
import com.unisinos.taskmanager.model.enums.TaskPriority;
import com.unisinos.taskmanager.model.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>,
        JpaSpecificationExecutor<Task> {

  List<Task> findByBoardId(UUID boardId);

  List<Task> findByAssignedUserId(UUID userId);

  List<Task> findByBoardIdAndStatus(UUID boardId, TaskStatus status);

  List<Task> findByBoardIdAndPriority(UUID boardId, TaskPriority priority);

  List<Task> findByBoardIdAndAssignedUserId(UUID boardId, UUID userId);
}