package com.b02.peep_it.repository;

import com.b02.peep_it.domain.Town;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TownRepository extends JpaRepository<Town, Long> {
}
