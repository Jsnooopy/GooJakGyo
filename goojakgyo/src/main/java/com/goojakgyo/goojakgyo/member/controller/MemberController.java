package com.goojakgyo.goojakgyo.member.controller;

import com.goojakgyo.goojakgyo.common.earth.JwtTokenProvider;
import com.goojakgyo.goojakgyo.member.domain.Member;
import com.goojakgyo.goojakgyo.member.dto.MemberLoginReqDto;
import com.goojakgyo.goojakgyo.member.dto.MemberSaveReqDto;
import com.goojakgyo.goojakgyo.member.service.MemberService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/member")
public class MemberController {

  private final MemberService memberService;
  private final JwtTokenProvider jwtTokenProvider;

  public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
    this.memberService = memberService;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @PostMapping("/create")
  public ResponseEntity<?> memberCreate(@RequestBody MemberSaveReqDto memberSaveReqDto) {
    Member member = memberService.create(memberSaveReqDto);
    return new ResponseEntity<>(member.getId(), HttpStatus.CREATED);
  }

  @PostMapping("/doLogin")
  public ResponseEntity<?> doLogin(@RequestBody MemberLoginReqDto memberLoginReqDto) {
    // email, password 검증
    Member member = memberService.login(memberLoginReqDto);

    // 일치할 경우 access token 발행
    String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());
    Map<String, Object> logInfo = new HashMap<>();
    logInfo.put("id", member.getId());
    logInfo.put("token", jwtToken);

    return new ResponseEntity<>(logInfo, HttpStatus.OK);
  }

}
