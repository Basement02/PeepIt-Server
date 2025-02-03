package com.b02.peep_it.repository;

import com.b02.peep_it.domain.Peep;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PeepRepository extends JpaRepository<Peep, Long> {
    Optional<Peep> findById(Long peepId);
    Page<Peep> findAllByMember_Id(String memberId, Pageable pageable);
}