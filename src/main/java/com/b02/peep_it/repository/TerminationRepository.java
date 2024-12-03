package com.b02.peep_it.repository;

import com.b02.peep_it.domain.Termination;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TerminationRepository extends JpaRepository<Termination, Long> {
}
