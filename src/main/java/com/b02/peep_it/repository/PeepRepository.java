package com.b02.peep_it.repository;

import com.b02.peep_it.domain.Peep;
import com.b02.peep_it.domain.State;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PeepRepository extends JpaRepository<Peep, Long> {
    Optional<Peep> findById(Long peepId);
    Page<Peep> findAllByMember_Id(String memberId, Pageable pageable);
    List<Peep> findAllByMember_Id(String memberId);
    Page<Peep> findAllByActiveTimeAfter(LocalDateTime cutoffTime, Pageable pageable);
    Page<Peep> findAllByActiveTimeAfterAndMember_Id(LocalDateTime cutoffTime, String memberId, Pageable pageable);
    Page<Peep> findAllByCodeAndActiveTimeAfter(
            State code, LocalDateTime cutoffTime, Pageable pageable
    );
    @Query(value = "SELECT p FROM Peep p JOIN p.peepLocation l " +
            "WHERE p.activeTime >= :cutoffTime " +
            "AND p.code.code = :code " +
            "AND ST_Distance(ST_GeomFromText(CONCAT('POINT(', l.longitude, ' ', l.latitude, ')'), 4326), " +
            "                   ST_GeomFromText(CONCAT('POINT(', :longitude, ' ', :latitude, ')'), 4326)) <= :distance " +
            "ORDER BY p.activeTime DESC")
    Page<Peep> findNearbyPeeps(@Param("latitude") double latitude,
                               @Param("longitude") double longitude,
                               @Param("distance") int distance,
                               @Param("cutoffTime") LocalDateTime cutoffTime,
                               @Param("code") String code,
                               Pageable pageable);

    @Query("""
        SELECT p FROM Peep p
        JOIN p.peepLocation l
        WHERE p.activeTime >= :cutoffTime
        AND p.code.code = :code
        AND ST_Distance(
            ST_GeomFromText(CONCAT('POINT(', :latitude, ' ', :longitude, ')'), 4326),
            ST_GeomFromText(CONCAT('POINT(', l.latitude, ' ', l.longitude, ')'), 4326)
        ) <= :distance
    """)
    List<Peep> findAllNearbyPeeps(
            @Param("longitude") double longitude,
            @Param("latitude") double latitude,
            @Param("distance") int distance,
            @Param("cutoffTime") LocalDateTime cutoffTime,
            @Param("code") String code
    );

}