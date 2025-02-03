package com.b02.peep_it.common.security.token;

import com.b02.peep_it.dto.member.MemberDto;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Date;
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

    public String createAccessToken(MemberDto memberDto) {
        Claims claims = Jwts.claims();
        claims.put("uid", memberDto.id());
        claims.put("role", memberDto.role().name());
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenTime))
                .signWith(SignatureAlgorithm.HS256, jwtSecretKey)
                .compact();
    }

    public String createRefreshToken(MemberDto memberDto) {
        Claims claims = Jwts.claims();
        claims.put("uid", memberDto.id());
        Date now = new Date();
        String refreshToken =  Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenTime))
                .signWith(SignatureAlgorithm.HS256, jwtSecretKey)
                .compact();

        updateUserRefreshToken(memberDto, refreshToken);
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
    public void updateUserRefreshToken(MemberDto memberDto, String refreshToken) {
        // Redis의 set 명령은 지정된 키에 대해 새로운 값을 설정하면서, 기존 값이 있을 경우 자동으로 대체
        // 기존 토큰 삭제 불필요
        stringRedisTemplate.opsForValue().set(String.valueOf(memberDto.id()), refreshToken, refreshTokenTime, TimeUnit.MILLISECONDS);
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
    - validateRegister: register token 유효성 검사 (null, exp, login, iss)
    - validateAccess: access token 유효성 검사 (null, exp, login, iss)
    - validateRefresh: refresh token 유효성 검사 (null, exp, login, iss)
     */

    public boolean validateAccessToken(String token) {
        if (!StringUtils.hasText(token)) {
//            throw new UnauthorizedException(ErrorCode.JWT_TOKEN_NOT_EXISTS);
            return false;
        }
        if(isLogout(token)) {
//            throw new UnauthorizedException(ErrorCode.LOG_OUT_JWT_TOKEN);
            return false;
        }
        if(!isIss(token)) {
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
        if(!isIss(token)) {
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
    Authentication 객체 생성
     */
    public Authentication getAuthentication(String token) {
        // 토큰 복호화
        Claims claims = getClaims(token);

        if (claims.get("role") == null) {
            throw new UnauthorizedException(CustomError.NEED_TO_CUSTOM);
        }

        // 클레임에서 권한 정보 취득
        String role = getRoleValueFromToken(token);

        // 권한 객체 생성
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

        // UserDetails 객체 생성
        UserDetails principal = new User(getUidfromToken(token), "", Collections.singleton(authority));

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

    public String getRoleValueFromToken(String token) {
        return getClaims(token).get("role").toString();
    }

    public Claims getClaims(String token) {
        return Jwts.parser().setSigningKey(jwtSecretKey).parseClaimsJws(token).getBody();
    }
}
