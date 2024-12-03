package com.b02.peep_it.repository;

import com.b02.peep_it.domain.PeepLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeepLocationRepository extends JpaRepository<PeepLocation, Long> {
}
