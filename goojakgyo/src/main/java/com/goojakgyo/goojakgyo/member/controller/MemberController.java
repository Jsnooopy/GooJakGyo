package com.goojakgyo.goojakgyo.member.controller;

import com.goojakgyo.goojakgyo.member.domain.Member;
import com.goojakgyo.goojakgyo.member.domain.SocialType;
import com.goojakgyo.goojakgyo.member.dto.response.MemberListResDto;
import com.goojakgyo.goojakgyo.member.dto.request.MemberLoginReqDto;
import com.goojakgyo.goojakgyo.member.dto.response.MemberProfileResDto;
import com.goojakgyo.goojakgyo.member.dto.request.MemberSaveReqDto;
import com.goojakgyo.goojakgyo.member.dto.request.OauthSaveReqDto;
import com.goojakgyo.goojakgyo.member.dto.request.RedirectDto;
import com.goojakgyo.goojakgyo.member.dto.profile.SocialProfile;
import com.goojakgyo.goojakgyo.member.dto.response.LoginResDto;
import com.goojakgyo.goojakgyo.member.service.auth.AuthService;
import com.goojakgyo.goojakgyo.member.service.member.MemberCommandService;
import com.goojakgyo.goojakgyo.member.service.member.MemberQueryService;
import com.goojakgyo.goojakgyo.member.service.oauth.OAuthLoginService;
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

  private final AuthService authService;
  private final OAuthLoginService oauthLoginService;
  private final MemberCommandService memberCommandService;
  private final MemberQueryService memberQueryService;

  public MemberController(AuthService authService,
      OAuthLoginService oauthLoginService, MemberCommandService memberCommandService,
      MemberQueryService memberQueryService) {
    this.authService = authService;
    this.oauthLoginService = oauthLoginService;
    this.memberCommandService = memberCommandService;
    this.memberQueryService = memberQueryService;
  }

  // 회원 가입
  @PostMapping("/create")
  public ResponseEntity<?> memberCreate(@RequestBody MemberSaveReqDto memberSaveReqDto) {
    Member member = memberCommandService.create(memberSaveReqDto);
    return new ResponseEntity<>(member.getId(), HttpStatus.CREATED);
  }

  @PostMapping("/oauth/create")
  public ResponseEntity<?> oauthCreate(@RequestBody OauthSaveReqDto oauthSaveReqDto) {
    Member member = memberCommandService.createOauth(oauthSaveReqDto);

    LoginResDto loginResDto = authService.issueTokens(member);

    ResponseCookie cookie = ResponseCookie.from("refreshToken", loginResDto.getRefreshToken())
        .httpOnly(true)
        .secure(false) // https 아니면 쿠키가 안들어가므로 개발중엔 false
        .sameSite("Strict")
        .path("/")
        .maxAge(Duration.ofDays(3))
        .build();

    Map<String, Object> loginInfo = new HashMap<>();
    loginInfo.put("memberId", loginResDto.getId());
    loginInfo.put("accessToken", loginResDto.getAccessToken());
    loginInfo.put("name", loginResDto.getName());

    URI location = URI.create("/member/" + member.getId());
    return ResponseEntity.created(location).header(HttpHeaders.SET_COOKIE, cookie.toString()).body(loginInfo);
  }
  
  // 로그인
  @PostMapping("/doLogin")
  public ResponseEntity<?> doLogin(@RequestBody MemberLoginReqDto memberLoginReqDto) {
    // email, password 검증 후 Access Token 및 Refresh Token 발급
    LoginResDto loginResDto = authService.login(memberLoginReqDto);

    //Refresh Token 쿠키로 설정
    ResponseCookie cookie = ResponseCookie.from("refreshToken", loginResDto.getRefreshToken())
        .httpOnly(true)
        .secure(false) // https 아니면 쿠키가 안들어가므로 개발중엔 false
        .sameSite("Strict")
        .path("/")
        .maxAge(Duration.ofDays(3))
        .build();

    Map<String, Object> loginInfo = new HashMap<>();
    loginInfo.put("memberId", loginResDto.getId());
    loginInfo.put("accessToken", loginResDto.getAccessToken());
    loginInfo.put("name", loginResDto.getName());

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(loginInfo);
  }

  @PostMapping("/{provider}/doLogin")
  public ResponseEntity<?> oauthLogin(@PathVariable String provider, @RequestBody RedirectDto redirectDto) {
    SocialType socialType = SocialType.valueOf(provider.toUpperCase());

    // access token 발급 및 사용자 정보 얻기
    // 회원가입이 되어 있다면 Access Token 및 Refresh Token 발급
    var result = oauthLoginService.loginOrCheck(redirectDto.getCode(), socialType);

    // 회원가입이 되어 있지 않다면 추가 정보 입력 후 회원가입
    if (result.isNewUser()) {
      SocialProfile profile = result.getSocialProfile();

      return ResponseEntity.ok(Map.of(
          "status", "NEED_OAUTH_CREATE",
          "socialId", profile.getSocialId(),
          "socialType", profile.getSocialType(),
          "name", profile.getName(),
          "email", profile.getEmail()
      ));
    }

    LoginResDto loginResDto = result.getLoginResDto();

    //Refresh Token 쿠키로 설정
    ResponseCookie cookie = ResponseCookie.from("refreshToken", loginResDto.getRefreshToken())
        .httpOnly(true)
        .secure(false) // https 아니면 쿠키가 안들어가므로 개발중엔 false
        .sameSite("Strict")
        .path("/")
        .maxAge(Duration.ofDays(3))
        .build();

    Map<String, Object> loginInfo = new HashMap<>();
    loginInfo.put("memberId", loginResDto.getId());
    loginInfo.put("accessToken", loginResDto.getAccessToken());
    loginInfo.put("name", loginResDto.getName());

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(loginInfo);
  }

  // Token 재발급
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

    LoginResDto loginResDto;
    try {
      loginResDto = authService.reissueTokens(refreshToken);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(401).body(e.getMessage());
    }

    // 새 Refresh Token을 쿠키로 재설정
    ResponseCookie cookie = ResponseCookie.from("refreshToken", loginResDto.getRefreshToken())
        .httpOnly(true)
        .secure(false)
        .sameSite("Strict")
        .path("/")
        .maxAge(Duration.ofDays(3))
        .build();

    Map<String, Object> token = new HashMap<>();
    token.put("accessToken", loginResDto.getAccessToken());

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(token);
  }

  // 로그아웃
  @PostMapping("/logout")
  public ResponseEntity<?> logout(@RequestBody Map<String, Long> request) {
    authService.logout(request.get("memberId"));

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

  // 회원 조회
  @GetMapping("/list/mentor")
  public ResponseEntity<?> mentorList() {
    List<MemberListResDto> dtos = memberQueryService.findMentors();
    return new ResponseEntity<>(dtos, HttpStatus.OK);
  }

  @GetMapping("/list/mentee")
  public ResponseEntity<?> menteeList() {
    List<MemberListResDto> dtos = memberQueryService.findMentees();
    return new ResponseEntity<>(dtos, HttpStatus.OK);
  }

  @GetMapping("/{memberId}")
  public ResponseEntity<?> memberProfile(@PathVariable Long memberId) {
    MemberProfileResDto dto = memberQueryService.getMemberProfile(memberId);
    return new ResponseEntity<>(dto, HttpStatus.OK);
  }
}
