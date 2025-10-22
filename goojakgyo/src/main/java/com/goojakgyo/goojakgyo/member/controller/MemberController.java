package com.goojakgyo.goojakgyo.member.controller;

import com.goojakgyo.goojakgyo.common.earth.JwtTokenProvider;
import com.goojakgyo.goojakgyo.member.domain.Member;
import com.goojakgyo.goojakgyo.member.domain.SocialType;
import com.goojakgyo.goojakgyo.member.dto.AccessTokenDto;
import com.goojakgyo.goojakgyo.member.dto.GoogleProfileDto;
import com.goojakgyo.goojakgyo.member.dto.KakaoProfileDto;
import com.goojakgyo.goojakgyo.member.dto.MemberListReqDto;
import com.goojakgyo.goojakgyo.member.dto.MemberLoginReqDto;
import com.goojakgyo.goojakgyo.member.dto.MemberSaveReqDto;
import com.goojakgyo.goojakgyo.member.dto.NaverProfileDto;
import com.goojakgyo.goojakgyo.member.dto.OauthSaveReqDto;
import com.goojakgyo.goojakgyo.member.dto.RedirectDto;
import com.goojakgyo.goojakgyo.member.service.GoogleService;
import com.goojakgyo.goojakgyo.member.service.KakaoService;
import com.goojakgyo.goojakgyo.member.service.MemberService;
import com.goojakgyo.goojakgyo.member.service.NaverService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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

  public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider,
      GoogleService googleService, KakaoService kakaoService, NaverService naverService) {
    this.memberService = memberService;
    this.jwtTokenProvider = jwtTokenProvider;
    this.googleService = googleService;
    this.kakaoService = kakaoService;
    this.naverService = naverService;
  }

  @PostMapping("/create")
  public ResponseEntity<?> memberCreate(@RequestBody MemberSaveReqDto memberSaveReqDto) {
    Member member = memberService.create(memberSaveReqDto);
    return new ResponseEntity<>(member.getId(), HttpStatus.CREATED);
  }

  @PostMapping("/oauth/create")
  public ResponseEntity<?> oauthCreate(@RequestBody OauthSaveReqDto oauthSaveReqDto) {
    Member member = memberService.createOauth(oauthSaveReqDto);
    String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());

    Map<String, Object> loginInfo = new HashMap<>();
    loginInfo.put("id", member.getId());
    loginInfo.put("token", jwtToken);

    return new ResponseEntity<>(loginInfo, HttpStatus.CREATED);
  }

  @PostMapping("/doLogin")
  public ResponseEntity<?> doLogin(@RequestBody MemberLoginReqDto memberLoginReqDto) {
    // email, password 검증
    Member member = memberService.login(memberLoginReqDto);

    // 일치할 경우 access token 발행
    String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());
    Map<String, Object> loginInfo = new HashMap<>();
    loginInfo.put("id", member.getId());
    loginInfo.put("token", jwtToken);

    return new ResponseEntity<>(loginInfo, HttpStatus.OK);
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

    // 회원가입이 되어 있으면 Token 발급
    String jwtToken = jwtTokenProvider.createToken(originalMember.getEmail(), originalMember.getRole().toString());
    Map<String, Object> loginInfo = new HashMap<>();
    loginInfo.put("id", originalMember.getId());
    loginInfo.put("token", jwtToken);

    return new ResponseEntity<>(loginInfo, HttpStatus.OK);
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

    // 회원가입이 되어 있으면 Token 발급
    String jwtToken = jwtTokenProvider.createToken(originalMember.getEmail(), originalMember.getRole().toString());
    Map<String, Object> loginInfo = new HashMap<>();
    loginInfo.put("id", originalMember.getId());
    loginInfo.put("token", jwtToken);

    return new ResponseEntity<>(loginInfo, HttpStatus.OK);
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

    // 회원 가입이 되어 있으면 Token 발급
    String jwtToken = jwtTokenProvider.createToken(originalMember.getEmail(), originalMember.getRole().toString());
    Map<String, Object> loginInfo = new HashMap<>();
    loginInfo.put("id", originalMember.getId());
    loginInfo.put("token", jwtToken);

    return new ResponseEntity<>(loginInfo, HttpStatus.OK);
  }

  @GetMapping("/list")
  public ResponseEntity<?> memberList() {
    List<MemberListReqDto> dtos = memberService.findAll();
    return new ResponseEntity<>(dtos, HttpStatus.OK);
  }

}
