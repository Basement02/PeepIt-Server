package com.b02.peep_it.repository;

import com.b02.peep_it.domain.Peep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeepRepository extends JpaRepository<Peep, Long> {
}
