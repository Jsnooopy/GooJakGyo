package com.goojakgyo.goojakgyo.member.service;

import com.goojakgyo.goojakgyo.member.domain.Member;
import com.goojakgyo.goojakgyo.member.dto.MemberLoginReqDto;
import com.goojakgyo.goojakgyo.member.dto.MemberSaveReqDto;
import com.goojakgyo.goojakgyo.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class MemberService {
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
    this.memberRepository = memberRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public Member create(MemberSaveReqDto memberSaveReqDto) {
    // 이미 가입되어 있는 이메일 검증
    if (memberRepository.findByEmail(memberSaveReqDto.getEmail()).isPresent()) {
      throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
    }

    Member newMember = Member.builder()
        .name(memberSaveReqDto.getName())
        .email(memberSaveReqDto.getEmail())
        .password(passwordEncoder.encode(memberSaveReqDto.getPassword()))
        .build();

    return memberRepository.save(newMember);
  }

  public Member login(MemberLoginReqDto memberLoginReqDto) {
    Member member = memberRepository.findByEmail(memberLoginReqDto.getEmail())
        .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 이메일 입니다."));

    if (!passwordEncoder.matches(memberLoginReqDto.getPassword(), member.getPassword())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    return member;
  }

}
