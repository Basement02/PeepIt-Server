package com.b02.peep_it.repository;

import com.b02.peep_it.domain.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StateRepository extends JpaRepository<State, Long> {
    Optional<State> findByCode(String code);
}
