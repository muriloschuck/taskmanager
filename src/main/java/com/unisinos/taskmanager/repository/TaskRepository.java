package com.unisinos.taskmanager.repository;

import com.unisinos.taskmanager.model.Task;
import com.unisinos.taskmanager.model.enums.TaskPriority;
import com.unisinos.taskmanager.model.enums.TaskStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>,
        JpaSpecificationExecutor<Task> {

  @EntityGraph(attributePaths = {"assignedUser", "board"})
  List<Task> findByBoard_Id(UUID boardId);

  @EntityGraph(attributePaths = {"assignedUser", "board"})
  List<Task> findByAssignedUser_Id(UUID userId);

  @EntityGraph(attributePaths = {"assignedUser", "board"})
  List<Task> findByBoard_IdAndStatus(UUID boardId, TaskStatus status);

  @EntityGraph(attributePaths = {"assignedUser", "board"})
  List<Task> findByBoard_IdAndPriority(UUID boardId, TaskPriority priority);

  @EntityGraph(attributePaths = {"assignedUser", "board"})
  List<Task> findByBoard_IdAndAssignedUser_Id(UUID boardId, UUID userId);
}