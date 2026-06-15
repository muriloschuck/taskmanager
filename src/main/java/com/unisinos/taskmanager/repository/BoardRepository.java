package com.unisinos.taskmanager.repository;

import com.unisinos.taskmanager.model.Board;
import com.unisinos.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BoardRepository extends JpaRepository<Board, UUID> {

  List<Board> findByOwner(User owner);

  List<Board> findByOwnerId(UUID ownerId);
}