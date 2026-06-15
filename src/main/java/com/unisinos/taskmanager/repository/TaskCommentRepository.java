package com.unisinos.taskmanager.repository;

import com.unisinos.taskmanager.model.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, UUID> {

  List<TaskComment> findByTaskId(UUID taskId);

  List<TaskComment> findByTaskIdOrderByCreatedAtAsc(UUID taskId);

  List<TaskComment> findByUserId(UUID userId);
}