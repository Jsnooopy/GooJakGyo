package com.goojakgyo.goojakgyo.member.controller;

import com.goojakgyo.goojakgyo.common.earth.JwtTokenProvider;
import com.goojakgyo.goojakgyo.member.domain.Member;
import com.goojakgyo.goojakgyo.member.domain.RefreshToken;
import com.goojakgyo.goojakgyo.member.domain.SocialType;
import com.goojakgyo.goojakgyo.member.dto.AccessTokenDto;
import com.goojakgyo.goojakgyo.member.dto.GoogleProfileDto;
import com.goojakgyo.goojakgyo.member.dto.KakaoProfileDto;
import com.goojakgyo.goojakgyo.member.dto.MemberListResDto;
import com.goojakgyo.goojakgyo.member.dto.MemberLoginReqDto;
import com.goojakgyo.goojakgyo.member.dto.MemberProfileResDto;
import com.goojakgyo.goojakgyo.member.dto.MemberSaveReqDto;
import com.goojakgyo.goojakgyo.member.dto.NaverProfileDto;
import com.goojakgyo.goojakgyo.member.dto.OauthSaveReqDto;
import com.goojakgyo.goojakgyo.member.dto.RedirectDto;
import com.goojakgyo.goojakgyo.member.repository.RefreshTokenRepository;
import com.goojakgyo.goojakgyo.member.service.GoogleService;
import com.goojakgyo.goojakgyo.member.service.KakaoService;
import com.goojakgyo.goojakgyo.member.service.MemberService;
import com.goojakgyo.goojakgyo.member.service.NaverService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/member")
public class MemberController {

  private final MemberService memberService;
  private final JwtTokenProvider jwtTokenProvider;
  private final GoogleService googleService;
  private final KakaoService kakaoService;
  private final NaverService naverService;
  private final RefreshTokenRepository refreshTokenRepository;

  public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider,
      GoogleService googleService, KakaoService kakaoService, NaverService naverService,
      RefreshTokenRepository refreshTokenRepository) {
    this.memberService = memberService;
    this.jwtTokenProvider = jwtTokenProvider;
    this.googleService = googleService;
    this.kakaoService = kakaoService;
    this.naverService = naverService;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  @PostMapping("/create")
  public ResponseEntity<?> memberCreate(@RequestBody MemberSaveReqDto memberSaveReqDto) {
    Member member = memberService.create(memberSaveReqDto);
    return new ResponseEntity<>(member.getId(), HttpStatus.CREATED);
  }

  @PostMapping("/oauth/create")
  public ResponseEntity<?> oauthCreate(@RequestBody OauthSaveReqDto oauthSaveReqDto) {
    Member member = memberService.createOauth(oauthSaveReqDto);

    String accessToken = jwtTokenProvider.createAccessToken(member.getEmail(), member.getRole().toString());
    String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail());

    // Redis 저장
    RefreshToken redisRefreshToken = RefreshToken.builder()
        .memberId(member.getId())
        .token(refreshToken)
        .expiration(60 * 60 * 24 * 3 * 1000L)
        .build();

    refreshTokenRepository.save(redisRefreshToken);

    ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)
        .secure(false) // https 아니면 쿠키가 안들어가므로 개발중엔 false
        .sameSite("Strict")
        .path("/")
        .maxAge(Duration.ofDays(3))
        .build();

    Map<String, Object> loginInfo = new HashMap<>();
    loginInfo.put("memberId", member.getId());
    loginInfo.put("accessToken", accessToken);

    URI location = URI.create("/member/" + member.getId());
    return ResponseEntity.created(location).header(HttpHeaders.SET_COOKIE, cookie.toString()).body(loginInfo);
  }

  @PostMapping("/doLogin")
  public ResponseEntity<?> doLogin(@RequestBody MemberLoginReqDto memberLoginReqDto) {
    // email, password 검증
    Member member = memberService.login(memberLoginReqDto);

    // 일치할 경우 access token & refresh token 발행
    String accessToken = jwtTokenProvider.createAccessToken(member.getEmail(), member.getRole().toString());
    String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail());

    // Redis 저장
    RefreshToken redisRefreshToken = RefreshToken.builder()
        .memberId(member.getId())
        .token(refreshToken)
        .expiration(60 * 60 * 24 * 3 * 1000L)
        .build();

    refreshTokenRepository.save(redisRefreshToken);

    ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)
        .secure(false) // https 아니면 쿠키가 안들어가므로 개발중엔 false
        .sameSite("Strict")
        .path("/")
        .maxAge(Duration.ofDays(3))
        .build();

    Map<String, Object> loginInfo = new HashMap<>();
    loginInfo.put("memberId", member.getId());
    loginInfo.put("accessToken", accessToken);

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(loginInfo);
  }

  @PostMapping("/google/doLogin")
  public ResponseEntity<?> googleLogin(@RequestBody RedirectDto redirectDto) {
    // AccessToken 발급
    AccessTokenDto accessTokenDto = googleService.getAccessToken(redirectDto.getCode());

    // 사용자 정보 얻기
    GoogleProfileDto googleProfileDto = googleService.getGoogleProfile(accessTokenDto.getAccess_token());

    // 회원가입이 되어 있지 않다면 추가 정보 입력 후 회원가입
    Member originalMember = memberService.getMemberBySocialId(googleProfileDto.getSub());
    if (originalMember == null){
      Map<String, Object> response = new HashMap<>();
      response.put("socialId", googleProfileDto.getSub());
      response.put("socialType", SocialType.GOOGLE);
      response.put("name", googleProfileDto.getName());
      response.put("email", googleProfileDto.getEmail());
      response.put("status", "NEED_OAUTH_CREATE");

      return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 회원가입이 되어 있으면 access token & refresh token 발급
    String accessToken = jwtTokenProvider.createAccessToken(originalMember.getEmail(), originalMember.getRole().toString());
    String refreshToken = jwtTokenProvider.createRefreshToken(originalMember.getEmail());

    // Redis 저장
    RefreshToken redisRefreshToken = RefreshToken.builder()
        .memberId(originalMember.getId())
        .token(refreshToken)
        .expiration(60 * 60 * 24 * 3 * 1000L)
        .build();

    refreshTokenRepository.save(redisRefreshToken);

    ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)
        .secure(false) // https 아니면 쿠키가 안들어가므로 개발중엔 false
        .sameSite("Strict")
        .path("/")
        .maxAge(Duration.ofDays(3))
        .build();

    Map<String, Object> loginInfo = new HashMap<>();
    loginInfo.put("memberId", originalMember.getId());
    loginInfo.put("accessToken", accessToken);

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(loginInfo);
  }

  @PostMapping("/kakao/doLogin")
  public ResponseEntity<?> kakaoLogin(@RequestBody RedirectDto redirectDto) {
    // Access Token 발급
    AccessTokenDto accessTokenDto = kakaoService.getAccessToken(redirectDto.getCode());

    // 사용자 정보 얻기
    KakaoProfileDto kakaoProfileDto = kakaoService.getKakaoProfile(accessTokenDto.getAccess_token());

    // 회원가입이 되어 있지 않다면 추가 정보 입력 후 회원 가입
    Member originalMember = memberService.getMemberBySocialId(kakaoProfileDto.getId());
    if (originalMember == null) {
      Map<String, Object> response = new HashMap<>();
      response.put("socialId", kakaoProfileDto.getId());
      response.put("socialType", SocialType.KAKAO);
      response.put("name", kakaoProfileDto.getKakao_account().getProfile().getNickname());
      response.put("email", kakaoProfileDto.getKakao_account().getEmail());
      response.put("status", "NEED_OAUTH_CREATE");

      return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 회원가입이 되어 있으면 access token & refresh token 발급
    String accessToken = jwtTokenProvider.createAccessToken(originalMember.getEmail(), originalMember.getRole().toString());
    String refreshToken = jwtTokenProvider.createRefreshToken(originalMember.getEmail());

    // Redis 저장
    RefreshToken redisRefreshToken = RefreshToken.builder()
        .memberId(originalMember.getId())
        .token(refreshToken)
        .expiration(60 * 60 * 24 * 3 * 1000L)
        .build();

    refreshTokenRepository.save(redisRefreshToken);

    ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)
        .secure(false) // https 아니면 쿠키가 안들어가므로 개발중엔 false
        .sameSite("Strict")
        .path("/")
        .maxAge(Duration.ofDays(3))
        .build();

    Map<String, Object> loginInfo = new HashMap<>();
    loginInfo.put("memberId", originalMember.getId());
    loginInfo.put("accessToken", accessToken);

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(loginInfo);
  }

  @PostMapping("/naver/doLogin")
  public ResponseEntity<?> naverLogin(@RequestBody RedirectDto redirectDto) {
    // Access Token 발급
    AccessTokenDto accessTokenDto = naverService.getAccessToken(redirectDto.getCode());

    // 사용자 정보 얻기
    NaverProfileDto naverProfileDto = naverService.getNaverProfile(accessTokenDto.getAccess_token());

    // 회원가입이 되어 있지 않다면 추가 정보 입력 후 회원가입
    Member originalMember = memberService.getMemberBySocialId(naverProfileDto.getResponse().getId());
    if (originalMember == null) {
      Map<String, Object> response = new HashMap<>();
      response.put("socialId", naverProfileDto.getResponse().getId());
      response.put("name", naverProfileDto.getResponse().getName());
      response.put("email", naverProfileDto.getResponse().getEmail());
      response.put("socialType", SocialType.NAVER);
      response.put("status", "NEED_OAUTH_CREATE");

      return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 회원 가입이 되어 있으면 access token & refresh token 발급
    String accessToken = jwtTokenProvider.createAccessToken(originalMember.getEmail(), originalMember.getRole().toString());
    String refreshToken = jwtTokenProvider.createRefreshToken(originalMember.getEmail());

    // Redis 저장
    RefreshToken redisRefreshToken = RefreshToken.builder()
        .memberId(originalMember.getId())
        .token(refreshToken)
        .expiration(60 * 60 * 24 * 3 * 1000L)
        .build();

    refreshTokenRepository.save(redisRefreshToken);

    ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)
        .secure(false) // https 아니면 쿠키가 안들어가므로 개발중엔 false
        .sameSite("Strict")
        .path("/")
        .maxAge(Duration.ofDays(3))
        .build();

    Map<String, Object> loginInfo = new HashMap<>();
    loginInfo.put("memberId", originalMember.getId());
    loginInfo.put("accessToken", accessToken);

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(loginInfo);
  }

  @GetMapping("/list/mentor")
  public ResponseEntity<?> mentorList() {
    List<MemberListResDto> dtos = memberService.findMentors();
    return new ResponseEntity<>(dtos, HttpStatus.OK);
  }

  @GetMapping("/list/mentee")
  public ResponseEntity<?> menteeList() {
    List<MemberListResDto> dtos = memberService.findMentees();
    return new ResponseEntity<>(dtos, HttpStatus.OK);
  }

  @GetMapping("/{memberId}")
  public ResponseEntity<?> memberProfile(@PathVariable Long memberId) {
    MemberProfileResDto dto = memberService.getMemberProfile(memberId);
    return new ResponseEntity<>(dto, HttpStatus.OK);
  }

  @PostMapping("/reissue")
  public ResponseEntity<?> reissue(HttpServletRequest request) {
    // 쿠키에서 Refresh Token 추출
    String refreshToken = null;
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("refreshToken".equals(cookie.getName())) {
          refreshToken = cookie.getValue();
          break;
        }
      }
    }

    if (refreshToken == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No Refresh Token in Cookie");
    }

    // Refresh Token 유효성 검증
    if (!jwtTokenProvider.validateToken(refreshToken)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Expired Refresh Token");
    }

    // Refresh Token에서 사용자 정보 추출
    String email = jwtTokenProvider.getEmailFromToken(refreshToken);
    Member member = memberService.getMemberByEmail(email);

    // Redis에서 저장된 토큰 조회
    RefreshToken storedToken = refreshTokenRepository.findById(member.getId())
        .orElse(null);

    if (storedToken == null || !storedToken.getToken().equals(refreshToken)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Refresh Token");
    }

    // 새로운 Access Token && Refresh Token 생성
    String newAccessToken = jwtTokenProvider.createAccessToken(email, member.getRole().toString());
    String newRefreshToken = jwtTokenProvider.createRefreshToken(email);

    // 기존 refresh 토큰 갱신 (TTL 다시 설정됨)
    storedToken.setToken(newRefreshToken);
    storedToken.setExpiration(60 * 60 * 24 * 3 * 1000L);
    refreshTokenRepository.save(storedToken);

    // 새 Refresh Token을 쿠키로 재설정
    ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken)
        .httpOnly(true)
        .secure(false)
        .sameSite("Strict")
        .path("/")
        .maxAge(Duration.ofDays(3))
        .build();

    Map<String, Object> token = new HashMap<>();
    token.put("accessToken", newAccessToken);

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(token);
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(@RequestBody Map<String, Long> request) {
    Long memberId = request.get("memberId");

    // Redis에서 Refresh Token 삭제
    refreshTokenRepository.deleteById(memberId);

    // 쿠키 즉시 만료
    ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
        .httpOnly(true)
        .secure(false)
        .sameSite("Strict")
        .path("/")
        .maxAge(0)
        .build();

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body("Logout Complete");
  }

}
