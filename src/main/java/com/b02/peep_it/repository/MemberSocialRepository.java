package com.b02.peep_it.repository;

import com.b02.peep_it.domain.MemberSocial;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberSocialRepository extends JpaRepository<MemberSocial, Long> {
}
