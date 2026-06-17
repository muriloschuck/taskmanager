package com.unisinos.taskmanager.repository;

import com.unisinos.taskmanager.model.TaskComment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, UUID> {

  @EntityGraph(attributePaths = {"user"})
  List<TaskComment> findByTask_Id(UUID taskId);

  @EntityGraph(attributePaths = {"user"})
  List<TaskComment> findByTask_IdOrderByCreatedAtAsc(UUID taskId);

  List<TaskComment> findByUser_Id(UUID userId);
}