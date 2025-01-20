package com.b02.peep_it.repository;

import com.b02.peep_it.domain.Member;
import com.b02.peep_it.domain.MemberSocial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {
    Optional<Member> findById(String id);

    Optional<Member> findByMemberSocial(MemberSocial memberSocial);

    Optional<Member> findByPhone(String phone);
}
