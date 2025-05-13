package com.b02.peep_it.repository;

import com.b02.peep_it.domain.MemberBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberBlockRepository extends JpaRepository<MemberBlock, Long> {
    @Query("SELECT mb FROM MemberBlock mb WHERE mb.blockerId.id = :id")
    Optional<MemberBlock> findByBlockerId(String id);
}