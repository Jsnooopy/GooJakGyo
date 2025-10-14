package com.goojakgyo.goojakgyo.member.controller;

// MemberController

import com.goojakgyo.goojakgyo.member.domain.Member;
import com.goojakgyo.goojakgyo.member.dto.MemberSaveReqDto;
import com.goojakgyo.goojakgyo.member.service.MemberService;
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

  public MemberController(MemberService memberService) {
    this.memberService = memberService;
  }

  @PostMapping("/create")
  public ResponseEntity<?> memberCreate(@RequestBody MemberSaveReqDto memberSaveReqDto) {
    Member member = memberService.create(memberSaveReqDto);
    return new ResponseEntity<>(member.getId(), HttpStatus.CREATED);
  }
}
