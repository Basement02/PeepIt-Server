package com.b02.peep_it.repository;

import com.b02.peep_it.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
