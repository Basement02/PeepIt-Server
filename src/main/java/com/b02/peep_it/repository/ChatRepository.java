package com.b02.peep_it.repository;

import com.b02.peep_it.domain.Chat;
import com.b02.peep_it.domain.Peep;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    /*
    EntityGraph: LAZY 로딩된 Peep을 즉시 로딩하여 N + 1 문제 방지
    DISTINCT: 중복 제거
    JOIN FETCH 대신 @EntityGraph를 사용한 이유 → 페이징 지원 (COUNT 문제 해결)
     */
    @EntityGraph(attributePaths = {"peep"})
    @Query("SELECT DISTINCT c.peep FROM Chat c WHERE c.member.id = :memberId")
    Page<Peep> findDistinctPeepsByMemberId(@Param("memberId") String memberId, Pageable pageable);
    List<Chat> findDistinctPeepsByMemberId(String memberId);

    List<Chat> findByPeepIdOrderByCreatedAtAsc(Long peepId);
}
