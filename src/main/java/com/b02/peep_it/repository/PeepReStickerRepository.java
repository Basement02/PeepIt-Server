package com.b02.peep_it.repository;

import com.b02.peep_it.domain.Peep;
import com.b02.peep_it.domain.PeepReSticker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface PeepReStickerRepository extends JpaRepository<PeepReSticker, Long> {
    // N + 1 문제 방지
    // JOIN FETCH는 페이징을 위한 COUNT 쿼리를 자동으로 생성하지 못하므로 EntityGraph 사용
//    @EntityGraph(attributePaths = {"peep"})
//    Page<Peep> findAllByMember_Id(String memberId, Pageable pageable);
    @Query("SELECT prs.peep FROM PeepReSticker prs WHERE prs.member.id = :memberId")
    Page<Peep> findAllByMember_Id(@Param("memberId") String memberId, Pageable pageable);
    List<PeepReSticker> findAllByMember_Id(String memberId);
}
