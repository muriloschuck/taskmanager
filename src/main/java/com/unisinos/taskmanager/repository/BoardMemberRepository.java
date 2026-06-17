package com.unisinos.taskmanager.repository;

import com.unisinos.taskmanager.model.BoardMember;
import com.unisinos.taskmanager.model.enums.BoardRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BoardMemberRepository extends JpaRepository<BoardMember, UUID> {

  @EntityGraph(attributePaths = {"user"})
  List<BoardMember> findByBoard_Id(UUID boardId);

  List<BoardMember> findByUser_Id(UUID userId);

  Optional<BoardMember> findByBoard_IdAndUser_Id(UUID boardId, UUID userId);

  boolean existsByBoard_IdAndUser_Id(UUID boardId, UUID userId);

  List<BoardMember> findByBoard_IdAndRole(UUID boardId, BoardRole role);
}