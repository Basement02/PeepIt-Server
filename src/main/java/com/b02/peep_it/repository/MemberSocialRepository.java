package com.b02.peep_it.repository;

import com.b02.peep_it.domain.MemberSocial;
import com.b02.peep_it.domain.constant.CustomProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberSocialRepository extends JpaRepository<MemberSocial, Long> {
//    @Query("SELECT ms FROM MemberSocial ms WHERE ms.provider = ?1 AND ms.providerId = ?2")
    Optional<MemberSocial> findByProviderAndProviderId(CustomProvider provider, String providerId);
}
