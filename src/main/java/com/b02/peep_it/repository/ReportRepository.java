package com.b02.peep_it.repository;

import com.b02.peep_it.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
