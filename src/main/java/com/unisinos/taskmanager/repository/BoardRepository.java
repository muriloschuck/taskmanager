package com.unisinos.taskmanager.repository;

import com.unisinos.taskmanager.model.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BoardRepository extends JpaRepository<Board, UUID> {

  @EntityGraph(attributePaths = "owner")
  @Query("select distinct b from BoardMember bm join bm.board b where bm.user.id = :userId")
  List<Board> findBoardsByMemberId(@Param("userId") UUID userId);
}