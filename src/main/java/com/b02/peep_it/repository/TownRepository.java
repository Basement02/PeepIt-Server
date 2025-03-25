package com.b02.peep_it.repository;

import com.b02.peep_it.domain.Town;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TownRepository extends JpaRepository<Town, String> {
    // member의 id를 기준으로 Town 조회
    @Query("SELECT t FROM Town t WHERE t.member.id = :id")
    Optional<Town> findByMid(@Param("id") String id);
}