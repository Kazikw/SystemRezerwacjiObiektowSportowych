package com.github.kazikw.boisgo.repository;

import com.github.kazikw.boisgo.domain.FriendRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRelationRepository extends JpaRepository<FriendRelation, Long> {

    @Query("SELECT fr FROM FriendRelation fr WHERE fr.userA.id = :userId")
    List<FriendRelation> findAllByUserId(@Param("userId") Long userId);

    Optional<FriendRelation> findByUserA_IdAndUserB_Id(Long userAId, Long userBId);

    boolean existsByUserA_IdAndUserB_Id(Long userAId, Long userBId);
}