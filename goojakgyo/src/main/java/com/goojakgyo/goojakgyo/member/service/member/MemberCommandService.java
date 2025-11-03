package com.goojakgyo.goojakgyo.member.service.member;

import com.goojakgyo.goojakgyo.member.domain.Keyword;
import com.goojakgyo.goojakgyo.member.domain.Member;
import com.goojakgyo.goojakgyo.member.domain.MemberKeyword;
import com.goojakgyo.goojakgyo.member.dto.request.MemberSaveReqDto;
import com.goojakgyo.goojakgyo.member.dto.request.OauthSaveReqDto;
import com.goojakgyo.goojakgyo.member.repository.KeywordRepository;
import com.goojakgyo.goojakgyo.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class MemberCommandService {
  private final MemberRepository memberRepository;
  private final KeywordRepository keywordRepository;
  private final PasswordEncoder passwordEncoder;

  public MemberCommandService(MemberRepository memberRepository, KeywordRepository keywordRepository, PasswordEncoder passwordEncoder) {
    this.memberRepository = memberRepository;
    this.keywordRepository = keywordRepository;
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
        .univName(memberSaveReqDto.getUnivName())
        .major(memberSaveReqDto.getMajor())
        .studentId(memberSaveReqDto.getStudentId())
        .role(memberSaveReqDto.getRole())
        .build();

    List<Keyword> selectedKeywords = keywordRepository.findAllById(memberSaveReqDto.getKeywordIds());
    for (Keyword keyword : selectedKeywords) {
      MemberKeyword memberKeyword = MemberKeyword.of(newMember, keyword);
      newMember.addMemberKeyword(memberKeyword);
    }

    return memberRepository.save(newMember);
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
}
