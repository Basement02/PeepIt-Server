package com.b02.peep_it.repository;

import com.b02.peep_it.domain.State;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StateRepository extends JpaRepository<State, Long> {
}
