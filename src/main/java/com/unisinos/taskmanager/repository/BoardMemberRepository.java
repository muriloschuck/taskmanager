package com.unisinos.taskmanager.repository;

import com.unisinos.taskmanager.model.BoardMember;
import com.unisinos.taskmanager.model.enums.BoardRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BoardMemberRepository extends JpaRepository<BoardMember, UUID> {

  List<BoardMember> findByBoardId(UUID boardId);

  List<BoardMember> findByUserId(UUID userId);

  Optional<BoardMember> findByBoardIdAndUserId(UUID boardId, UUID userId);

  boolean existsByBoardIdAndUserId(UUID boardId, UUID userId);

  List<BoardMember> findByBoardIdAndRole(UUID boardId, BoardRole role);
}