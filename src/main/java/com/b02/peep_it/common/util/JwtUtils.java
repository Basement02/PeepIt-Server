package com.b02.peep_it.common.util;

import com.b02.peep_it.common.security.token.CustomUserDetails;
import com.b02.peep_it.domain.constant.CustomProvider;
import com.b02.peep_it.dto.member.CommonMemberDto;
import com.b02.peep_it.common.exception.CustomError;
import com.b02.peep_it.common.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtUtils {
    @Value("${jwt.time.register}")
    private long registerTokenTime; // 10분
    @Value("${jwt.time.access}")
    private long accessTokenTime; // 30일
    @Value("${jwt.time.refresh}")
    private long refreshTokenTime; // 30일
    @Value("${jwt.key}")
    private String jwtSecretKey;
    @Value("${jwt.issuer}")
    private String issuer;
    private final StringRedisTemplate stringRedisTemplate;


    /*
    토큰 생성
    - register token: provider, providerId, issuer
    - access token: uid, role, issuer
    - refresh token: uid, issuer
     */
    public String createRegisterToken(String provider, String providerId) {
        log.info("create RegisterToken");
        Claims claims = Jwts.claims();
        claims.put("provider", provider);
        claims.put("providerId", providerId);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + registerTokenTime))
                .signWith(SignatureAlgorithm.HS256, jwtSecretKey)
                .compact();
    }

    public String createAccessToken(CommonMemberDto commonMemberDto) {
        Claims claims = Jwts.claims();
        claims.put("uid", commonMemberDto.id());
        claims.put("role", commonMemberDto.role().name());
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenTime))
                .signWith(SignatureAlgorithm.HS256, jwtSecretKey)
                .compact();
    }

    public String createRefreshToken(CommonMemberDto commonMemberDto) {
        Claims claims = Jwts.claims();
        claims.put("uid", commonMemberDto.id());
        Date now = new Date();
        String refreshToken =  Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenTime))
                .signWith(SignatureAlgorithm.HS256, jwtSecretKey)
                .compact();

        updateUserRefreshToken(commonMemberDto, refreshToken);
        return refreshToken;
    }

    /*
    access token utils (access token은 블랙리스트 방식으로 운용
    - set blacklist: 남은 유효 시간 동안 블랙리스트 처리 (사용 불가 처리)
     */
    public void setBlackList(String accessToken) {
        Long expiration = getExpiration(accessToken);
        stringRedisTemplate.opsForValue().set(accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
    }

    /*
    refresh token utils (refresh token은 화이트리스트 방식으로 운용)
    - update: 새로운 값 설정
    - get: uid로 토큰 값 조회
    - delete: 토큰 만료 처리 (삭제)
     */
    public void updateUserRefreshToken(CommonMemberDto commonMemberDto, String refreshToken) {
        // Redis의 set 명령은 지정된 키에 대해 새로운 값을 설정하면서, 기존 값이 있을 경우 자동으로 대체
        // 기존 토큰 삭제 불필요
        stringRedisTemplate.opsForValue().set(String.valueOf(commonMemberDto.id()), refreshToken, refreshTokenTime, TimeUnit.MILLISECONDS);
    }

    public String getUserRefreshToken(String uid) {
        return stringRedisTemplate.opsForValue().get(uid);
    }

    public void deleteRefreshTokenById(String id) {
        // 로그아웃 등 토큰 무효화 시 사용
        if (getUserRefreshToken(id) != null) {
            stringRedisTemplate.delete(id);
        }
        else {
            throw new UnauthorizedException(CustomError.NEED_TO_CUSTOM);
        }
    }

    /*
    common token utils
    - validateRegister: register token 유효성 검사 (null, exp, login, iss), provider별 검증
    - validateAccess: access token 유효성 검사 (null, exp, login, iss)
    - validateRefresh: refresh token 유효성 검사 (null, exp, login, iss)
     */

    public boolean validateRegisterToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        if (!isIss(token)) {
            return false;
        }
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecretKey)
                    .parseClaimsJws(token)
                    .getBody();

            // 토큰에서 필드 추출
            CustomProvider provider = (CustomProvider) claims.get("provider");
            String providerId = (String) claims.get("providerId");

            // provider에 따른 social uid 추출 (추출 시, 유효성 검증 거침)
            String socialUid = getSocialUid(provider, providerId);

            return socialUid != null;


        } catch (MalformedJwtException e) {
            throw new UnauthorizedException(CustomError.NEED_TO_CUSTOM);
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(CustomError.NEED_TO_CUSTOM);
        } catch (UnauthorizedException e) {
            return false;
        }
    }

    public boolean validateAccessToken(String token) {
        if (!StringUtils.hasText(token)) {
//            throw new UnauthorizedException(ErrorCode.JWT_TOKEN_NOT_EXISTS);
            return false;
        }
        if (isLogout(token)) {
//            throw new UnauthorizedException(ErrorCode.LOG_OUT_JWT_TOKEN);
            return false;
        }
        if (!isIss(token)) {
            return false;
        }
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecretKey)
                    .parseClaimsJws(token)
                    .getBody();

            if (claims.get("uid") != null && claims.get("role") != null) {
                return true;
            } else {
//                throw new IllegalArgumentException("Unknown token type.");
                return false;
            }
        } catch (MalformedJwtException e) {
            throw new UnauthorizedException(CustomError.NEED_TO_CUSTOM);
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(CustomError.NEED_TO_CUSTOM);
        } catch (UnauthorizedException e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        if (!isIss(token)) {
            return false;
        }
        try {
            Claims claims = getClaims(token);
            String uid = claims.get("uid").toString();
            if (uid != null) {
                return true;
            }
        } catch (MalformedJwtException e) {
//            throw new UnauthorizedException(CustomError.NEED_TO_CUSTOM);
            return false;
        } catch (ExpiredJwtException e) {
//            throw new UnauthorizedException(CustomError.NEED_TO_CUSTOM);
            return false;
        } catch (UnauthorizedException e) {
            return false;
        }
        return false;
    }

    // Register Token 검증
    private boolean validateRegisterToken(Claims claims) {
        return issuer.equals(claims.getIssuer());
    }

    // Refresh Token 검증
    private boolean validateRefreshToken(Claims claims) {
        return issuer.equals(claims.getIssuer());
    }

    /*
    id token으로 소셜 고유 ID 조회
    - provider별 idToken 유효성 검사
    - provider별 고유 ID 조회 및 반환
        - kakao
        - naver
        - apple
     */
    public String getSocialUid(CustomProvider provider, String providerId) {
        String socialUid = "";
        if (validateIdToken(provider, providerId) == false) {
            log.info("유효하지 않은 id token");
            throw new UnauthorizedException(CustomError.NEED_TO_CUSTOM);
        }
        // kakao
        if (provider.equals(CustomProvider.KAKAO)) {

        }

        // naver
        if (provider.equals(CustomProvider.NAVER)) {

        }

        // apple
        if (provider.equals(CustomProvider.APPLE)) {

        }

        // tester
        if (provider.equals(CustomProvider.TESTER)) {
            // 테스터 계정 생성 시, 입력된 값으로 고유 ID를 대체
            socialUid = providerId;
        }

        return socialUid;
    }

    /*
    idToken 검증
    - provider별 idToken 유효성 검증
        - kakao
        - naver
        - apple
        - tester
     */
    public boolean validateIdToken(CustomProvider provider, String providerId) {
        // kakao
        if (provider.equals(CustomProvider.KAKAO)) {
            return true;
        }

        // naver
        if (provider.equals(CustomProvider.NAVER)) {
            return true;
        }

        // apple
        if (provider.equals(CustomProvider.APPLE)) {
            return true;
        }

        // tester
        if (provider.equals(CustomProvider.TESTER)) {
            return true;
        }

        return false;
    }

    /*
    Authentication 객체 생성
    - getAuthentication: access token 활용
    - getTempAuthentication: register token 활용
     */
    public Authentication getAuthentication(String token) {
        // 토큰 복호화
        Claims claims = getClaims(token);

        if (claims.get("role") == null) {
            log.info("토큰에 role값 부재!!");
            throw new UnauthorizedException(CustomError.NEED_TO_CUSTOM);
        }

        String uid = claims.get("uid").toString(); // 사용자 ID
        String username = claims.getSubject(); // 닉네임
        String role = claims.get("role").toString();
        String provider = "";
        String providerId = "";

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

        // CustomUserDetails 사용
        CustomUserDetails principal = CustomUserDetails.builder()
                .username(username)
                .uid(uid)
                .provider(provider)
                .providerId(providerId)
                .authorities(Collections.singleton(authority))
                .build();

        // Authentication 반환
        return new UsernamePasswordAuthenticationToken(principal, "", Collections.singleton(authority));
    }

    public Authentication getTempAuthentication(String token) {
        // 토큰 복호화
        Claims claims = getClaims(token);

        String uid = "";
        String username = "";
        String role = "register";
        String provider = claims.get("provider").toString();
        String providerId = claims.get("providerId").toString();

        if (provider == null || providerId == null) {
            log.info("토큰에 문제가 있다!! provider || providerId 부재!!");
            throw new UnauthorizedException(CustomError.NEED_TO_CUSTOM);
        }

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

        // CustomUserDetails 사용
        CustomUserDetails principal = CustomUserDetails.builder()
                .username(username)
                .uid(uid)
                .provider(provider)
                .providerId(providerId)
                .authorities(Collections.singleton(authority))
                .build();

        // Authentication 반환
        return new UsernamePasswordAuthenticationToken(principal, "", Collections.singleton(authority));
    }

    public boolean isLogout(String accessToken) {
        return !ObjectUtils.isEmpty(stringRedisTemplate.opsForValue().get(accessToken));
    }

    public boolean isIss(String token) {
        return issuer.equals(token);
    }

    public Long getExpiration(String token) {
        Date expiration = getClaims(token).getExpiration();
        return expiration.getTime() - new Date().getTime();
    }

    public String getUidfromToken(String token) {
        return getClaims(token).get("uid").toString();
    }

    public Claims getClaims(String token) {
        return Jwts.parser().setSigningKey(jwtSecretKey).parseClaimsJws(token).getBody();
    }
}
