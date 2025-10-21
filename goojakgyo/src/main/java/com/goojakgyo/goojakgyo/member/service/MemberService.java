package com.goojakgyo.goojakgyo.member.service;

import com.goojakgyo.goojakgyo.member.domain.Member;
import com.goojakgyo.goojakgyo.member.domain.SocialType;
import com.goojakgyo.goojakgyo.member.dto.MemberListReqDto;
import com.goojakgyo.goojakgyo.member.dto.MemberLoginReqDto;
import com.goojakgyo.goojakgyo.member.dto.MemberSaveReqDto;
import com.goojakgyo.goojakgyo.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
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

  public List<MemberListReqDto> findAll() {
    List<Member> members = memberRepository.findAll();
    List<MemberListReqDto> memberListReqDtos = new ArrayList<>();

    for (Member m : members) {
      MemberListReqDto memberListReqDto = new MemberListReqDto();
      memberListReqDto.setId(m.getId());
      memberListReqDto.setName(m.getName());
      memberListReqDto.setEmail(m.getEmail());
      memberListReqDtos.add(memberListReqDto);
    }

    return memberListReqDtos;
  }

  public Member getMemberBySocialId(String socialId) {
    Member member = memberRepository.findBySocialId(socialId).orElse(null);

    return member;
  }

  public Member createOauth(String socialId, String name, String email, SocialType socialType) {
    Member newMember = Member.builder()
        .name(name)
        .email(email)
        .socialType(socialType)
        .socialId(socialId)
        .build();

    return memberRepository.save(newMember);
  }

}
