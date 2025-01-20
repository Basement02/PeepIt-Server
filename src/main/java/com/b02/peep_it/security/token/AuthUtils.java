package com.b02.peep_it.security.token;

import com.b02.peep_it.domain.Member;
import com.b02.peep_it.domain.constant.Role;
import com.b02.peep_it.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthUtils {

    // SecurityContextHolder에서 사용자 정보를 꺼내는 utils

    private final MemberRepository memberRepository;

    /*
    Member 객체 반환
     */
    public Member getCurrentMember() {
        return memberRepository.findById(getCurrentMemberUid()).get();
    }

    /*
    SecurityContext -> Authentication
     */
    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication;
    }

    /*
    Authentication -> Principal
     */
    private Object getPrincipal() {
        // 현재 사용자의 principal 가져오기
        return getAuthentication().getPrincipal();
    }

    public String getCurrentMemberUid() {
        Object principalObject = getPrincipal();

        // principal이 UserDetails 인스턴스인지 확인
        if (principalObject instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) principalObject;

            // UserDetails 인스턴스에서 uid 획득
            return userDetails.getUid();
        }
        return null;
    }

    public String getCurrentMemberName() {
        Object principalObject = getPrincipal();

        // principal이 UserDetails 인스턴스인지 확인
        if (principalObject instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) principalObject;

            // UserDetails 인스턴스에서 닉네임 획득
            return userDetails.getUsername();
        }
        return "Error Error Error";
    }

    public Role getCurrentUserRole() {
        Object principalObject = getPrincipal();

        // principal이 UserDetails 인스턴스인지 확인
        if (principalObject instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) principalObject;

            // UserDetails에서 권한 목록 가져오기
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
            GrantedAuthority firstAuthority = authorities.iterator().next();
            String authorityString = firstAuthority.getAuthority();

            // UserDetails 인스턴스에서 Role String 획득
            return Role.valueOf(authorityString);
        }

        return null;
    }
}