package com.goojakgyo.goojakgyo.member.service;

import com.goojakgyo.goojakgyo.member.domain.Keyword;
import com.goojakgyo.goojakgyo.member.domain.Member;
import com.goojakgyo.goojakgyo.member.domain.MemberKeyword;
import com.goojakgyo.goojakgyo.member.domain.Role;
import com.goojakgyo.goojakgyo.member.dto.MemberListResDto;
import com.goojakgyo.goojakgyo.member.dto.MemberLoginReqDto;
import com.goojakgyo.goojakgyo.member.dto.MemberProfileResDto;
import com.goojakgyo.goojakgyo.member.dto.MemberSaveReqDto;
import com.goojakgyo.goojakgyo.member.dto.OauthSaveReqDto;
import com.goojakgyo.goojakgyo.member.repository.KeywordRepository;
import com.goojakgyo.goojakgyo.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class MemberService {
  private final MemberRepository memberRepository;
  private final KeywordRepository keywordRepository;
  private final PasswordEncoder passwordEncoder;

  public MemberService(MemberRepository memberRepository, KeywordRepository keywordRepository, PasswordEncoder passwordEncoder) {
    this.memberRepository = memberRepository;
    this.keywordRepository = keywordRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public Member create(MemberSaveReqDto memberSaveReqDto) {
    // 이미 가입되어 있는 이메일 검증
    if (memberRepository.findByEmail(memberSaveReqDto.getEmail()).isPresent()) {
      throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
    }

    List<Keyword> selectedKeywords = keywordRepository.findAllById(memberSaveReqDto.getKeywordIds());

    Member newMember = Member.builder()
        .name(memberSaveReqDto.getName())
        .email(memberSaveReqDto.getEmail())
        .password(passwordEncoder.encode(memberSaveReqDto.getPassword()))
        .univName(memberSaveReqDto.getUnivName())
        .major(memberSaveReqDto.getMajor())
        .studentId(memberSaveReqDto.getStudentId())
        .role(memberSaveReqDto.getRole())
        .build();

    for (Keyword keyword : selectedKeywords) {
      MemberKeyword memberKeyword = MemberKeyword.of(newMember, keyword);
      newMember.addMemberKeyword(memberKeyword);
    }

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

  public List<MemberListResDto> findMentors() {
    List<Member> mentors = memberRepository.findByRole(Role.MENTOR);
    List<MemberListResDto> memberListResDtos = new ArrayList<>();

    for (Member m : mentors) {
      MemberListResDto memberListResDto = new MemberListResDto();
      memberListResDto.setId(m.getId());
      memberListResDto.setName(m.getName());
      memberListResDto.setEmail(m.getEmail());
      memberListResDto.setUnivName(m.getUnivName());
      memberListResDto.setMajor(m.getMajor());
      memberListResDtos.add(memberListResDto);
    }

    return memberListResDtos;
  }

  public List<MemberListResDto> findMentees() {
    List<Member> mentees = memberRepository.findByRole(Role.MENTEE);
    List<MemberListResDto> memberListResDtos = new ArrayList<>();

    for (Member m : mentees) {
      MemberListResDto memberListResDto = new MemberListResDto();
      memberListResDto.setId(m.getId());
      memberListResDto.setName(m.getName());
      memberListResDto.setEmail(m.getEmail());
      memberListResDto.setUnivName(m.getUnivName());
      memberListResDto.setMajor(m.getMajor());
      memberListResDtos.add(memberListResDto);
    }

    return memberListResDtos;
  }

  public Member getMemberBySocialId(String socialId) {
    Member member = memberRepository.findBySocialId(socialId).orElse(null);

    return member;
  }

  public Member createOauth(OauthSaveReqDto oauthSaveReqDto) {
    Member newMember = Member.builder()
        .name(oauthSaveReqDto.getName())
        .email(oauthSaveReqDto.getEmail())
        .socialType(oauthSaveReqDto.getSocialType())
        .socialId(oauthSaveReqDto.getSocialId())
        .univName(oauthSaveReqDto.getUnivName())
        .major(oauthSaveReqDto.getMajor())
        .studentId(oauthSaveReqDto.getStudentId())
        .role(oauthSaveReqDto.getRole())
        .build();

    List<Keyword> selectedKeywords = keywordRepository.findAllById(oauthSaveReqDto.getKeywordIds());
    for (Keyword keyword : selectedKeywords) {
      MemberKeyword memberKeyword = MemberKeyword.of(newMember, keyword);
      newMember.addMemberKeyword(memberKeyword);
    }

    return memberRepository.save(newMember);
  }

  public MemberProfileResDto getMemberProfile(Long memberId) {
    Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

    List<String> keywordNames = member.getMemberKeywords().stream().map(MemberKeyword::getKeyword).map(k -> k.getKeywordName()).collect(Collectors.toList());

    return MemberProfileResDto.builder()
        .id(member.getId())
        .name(member.getName())
        .email(member.getEmail())
        .profileImageUrl(member.getProfileImageUrl())
        .univName(member.getUnivName())
        .major(member.getMajor())
        .studentId(member.getStudentId())
        .keywords(keywordNames)
        .build();
  }

  public Member getMemberByEmail(String email) {
    return memberRepository.findByEmail(email).orElse(null);
  }
}
