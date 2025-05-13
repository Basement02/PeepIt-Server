package com.b02.peep_it.common.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.b02.peep_it.domain.constant.CustomProvider;
import com.b02.peep_it.dto.member.ResponseCommonMemberDto;
import com.b02.peep_it.common.exception.CustomError;
import com.b02.peep_it.common.exception.UnauthorizedException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.interfaces.RSAPublicKey;
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
    @Value("${auth.key.kakao}")
    private String KAKAO_REST_API_KEY = "";
    @Value("${auth.key.apple}")
    private String APPLE_CLIENT_ID = "";


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

    public String createAccessToken(ResponseCommonMemberDto responseCommonMemberDto) {
        Claims claims = Jwts.claims();
        claims.put("uid", responseCommonMemberDto.getId());
        claims.put("role", responseCommonMemberDto.getRole());
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenTime))
                .signWith(SignatureAlgorithm.HS256, jwtSecretKey)
                .compact();
    }

    public String createRefreshToken(ResponseCommonMemberDto responseCommonMemberDto) {
        Claims claims = Jwts.claims();
        claims.put("uid", responseCommonMemberDto.getId());
        Date now = new Date();
        String refreshToken =  Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenTime))
                .signWith(SignatureAlgorithm.HS256, jwtSecretKey)
                .compact();

        updateUserRefreshToken(responseCommonMemberDto, refreshToken);
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
    public void updateUserRefreshToken(ResponseCommonMemberDto responseCommonMemberDto, String refreshToken) {
        // Redis의 set 명령은 지정된 키에 대해 새로운 값을 설정하면서, 기존 값이 있을 경우 자동으로 대체
        // 기존 토큰 삭제 불필요
        stringRedisTemplate.opsForValue().set(String.valueOf(responseCommonMemberDto.getId()), refreshToken, refreshTokenTime, TimeUnit.MILLISECONDS);
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
            log.warn("❌ Token is empty or null");
            return false;
        }
        if (!isIss(token)) {
            log.warn("❌ Issuer mismatch in token");
            return false;
        }
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecretKey)
                    .parseClaimsJws(token)
                    .getBody();

            // 토큰에서 필드 추출
            log.info("✅ JWT Claims: {}", claims);
            CustomProvider provider = CustomProvider.valueOf((String) claims.get("provider"));
            String providerId = (String) claims.get("providerId");

            log.info("🔍 Extracted Provider: {}", provider);
            log.info("🔍 Extracted ProviderId: {}", providerId);

            // provider에 따른 social uid 추출 (추출 시, 유효성 검증 거침)
            String socialUid = getSocialUid(provider, providerId);

            if (socialUid == null) {
                log.warn("❌ Failed to extract social UID. Invalid providerId?");
            }

            return socialUid != null;

        } catch (MalformedJwtException e) {
            log.warn("❌ Malformed JWT: {}", e.getMessage());
            throw new UnauthorizedException(CustomError.NEED_TO_CUSTOM);
        } catch (ExpiredJwtException e) {
            log.warn("❌ Expired JWT: {}", e.getMessage());
            throw new UnauthorizedException(CustomError.NEED_TO_CUSTOM);
        } catch (UnauthorizedException e) {
            log.warn("❌ Unauthorized JWT: {}", e.getMessage());
            return false;
        } catch (IOException | InterruptedException e) {
            throw new UnauthorizedException(CustomError.NEED_TO_CUSTOM);
        }
    }


//    public boolean validateRegisterToken(String token) {
//        if (!StringUtils.hasText(token)) {
//            return false;
//        }
//        if (!isIss(token)) {
//            return false;
//        }
//        try {
//            Claims claims = Jwts.parser()
//                    .setSigningKey(jwtSecretKey)
//                    .parseClaimsJws(token)
//                    .getBody();
//
//            // 토큰에서 필드 추출
//            CustomProvider provider = (CustomProvider) claims.get("provider");
//            String providerId = (String) claims.get("providerId");
//
//            // provider에 따른 social uid 추출 (추출 시, 유효성 검증 거침)
//            String socialUid = getSocialUid(provider, providerId);
//
//            return socialUid != null;
//
//
//        } catch (MalformedJwtException e) {
//            throw new UnauthorizedException(CustomError.NEED_TO_CUSTOM);
//        } catch (ExpiredJwtException e) {
//            throw new UnauthorizedException(CustomError.NEED_TO_CUSTOM);
//        } catch (UnauthorizedException e) {
//            return false;
//        }
//    }

    public boolean validateAccessToken(String token) {
        if (!StringUtils.hasText(token)) {
//            throw new UnauthorizedException(ErrorCode.JWT_TOKEN_NOT_EXISTS);
            return false;
        }
        if (isLogout(token)) {
            log.info("로그아웃된 토큰");
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
    public String getSocialUid(CustomProvider provider, String providerId) throws IOException, InterruptedException {
        String socialUid = "";
        if (!validateIdToken(provider, providerId)) {
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
    public boolean validateIdToken(CustomProvider provider, String providerId) throws IOException, InterruptedException {
        // kakao
        if (provider.equals(CustomProvider.KAKAO)) {
            return validateKakaoIdToken(providerId);
        }

        // naver
        if (provider.equals(CustomProvider.NAVER)) {
            return validateNaverIdToken(providerId);
        }

        // apple
        if (provider.equals(CustomProvider.APPLE)) {
            return validateAppleIdToken(providerId);
        }

        // tester
        if (provider.equals(CustomProvider.TESTER)) {
            return true;
        }

        return false;
    }

    private boolean validateKakaoIdToken(String idToken) {
        // 1. https://kauth.kakao.com/.well-known/jwks.json
        // 2. JWT Header의 kid 사용해 해당 키 찾아서 RSAPublicKey 생성
        // 3. com.auth0.jwt로 서명 및 iss/aud 검증
        return verifyWithJWK(idToken, "https://kauth.kakao.com", "https://kauth.kakao.com/.well-known/jwks.json", KAKAO_REST_API_KEY);
    }

    private boolean validateNaverIdToken(String idToken) throws IOException, InterruptedException {
        // 1. 네이버는 idToken을 자체 검증하지 않고 사용자 정보 API 호출을 권장
        // 2. idToken을 Authorization 헤더에 담아 https://openapi.naver.com/v1/nid/me 호출
        // 3. 정상 응답 여부로 검증 (401 등 나오면 실패)
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://openapi.naver.com/v1/nid/me"))
                .header("Authorization", "Bearer " + idToken)
                .GET().build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200;
    }

    private boolean validateAppleIdToken(String idToken) {
        // 1. https://appleid.apple.com/auth/keys
        // 2. kid, alg 확인 → 공개키 변환
        // 3. iss == https://appleid.apple.com, aud == YOUR_APPLE_CLIENT_ID 확인
        return verifyWithJWK(idToken, "https://appleid.apple.com", "https://appleid.apple.com/auth/keys", APPLE_CLIENT_ID);
    }

    private boolean verifyWithJWK(String idToken, String expectedIssuer, String jwksUrl, String expectedAudience) {
        try {
            DecodedJWT jwt = JWT.decode(idToken);
            String kid = jwt.getKeyId();

            // JWKS 요청
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(jwksUrl))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("JWKS 요청 실패: {}", response.statusCode());
                return false;
            }

            String jwksJson = response.body();
            JsonNode jwks = new ObjectMapper().readTree(jwksJson).get("keys");

            for (JsonNode key : jwks) {
                if (!key.get("kid").asText().equals(kid)) continue;

                RSAPublicKey publicKey = JwkUtils.parseRSAPublicKey(key);  // JWK를 RSAPublicKey로 변환
                Algorithm algorithm = Algorithm.RSA256(publicKey, null);

                JWTVerifier verifier = JWT.require(algorithm)
                        .withIssuer(expectedIssuer)
                        .withAudience(expectedAudience)
                        .build();

                verifier.verify(idToken); // 여기서 예외 발생 시 검증 실패
                return true;
            }

            log.warn("kid {}에 해당하는 공개키를 찾지 못했습니다", kid);
        } catch (Exception e) {
            log.error("verifyWithJWK error: {}", e.getMessage());
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
//        String username = claims.getSubject(); // 닉네임
        String role = claims.get("role").toString();
        String provider = "";
        String providerId = "";

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

        // CustomUserDetails 사용
        CustomUserDetails principal = CustomUserDetails.builder()
//                .username(username)
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
//                .username(username)
                .uid(uid)
                .provider(provider)
                .providerId(providerId)
                .authorities(Collections.singleton(authority))
                .build();

        // Authentication 반환
        return new UsernamePasswordAuthenticationToken(principal, "", Collections.singleton(authority));
    }

    public boolean isLogout(String accessToken) {
        String key = "blacklist:" + accessToken;
        log.info("로그아웃된 토큰인가요? {}", key);
        log.info(String.valueOf(stringRedisTemplate.hasKey(key)));
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    public boolean isIss(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecretKey)
                    .parseClaimsJws(token)
                    .getBody();

            String actualIssuer = claims.getIssuer(); // ✅ JWT에서 iss 값 추출
            log.info("🔍 JWT Issuer 검증: 실제값={}, 기대값={}", actualIssuer, issuer);

            return issuer.equals(actualIssuer); // ✅ 기대하는 issuer 값과 비교
        } catch (Exception e) {
            log.warn("❌ Issuer 검증 실패: {}", e.getMessage());
            return false;
        }
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
