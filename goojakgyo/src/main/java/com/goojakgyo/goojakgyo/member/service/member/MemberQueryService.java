package com.goojakgyo.goojakgyo.member.service.member;

import com.goojakgyo.goojakgyo.member.domain.Member;
import com.goojakgyo.goojakgyo.member.domain.MemberKeyword;
import com.goojakgyo.goojakgyo.member.domain.Role;
import com.goojakgyo.goojakgyo.member.dto.response.MemberListResDto;
import com.goojakgyo.goojakgyo.member.dto.response.MemberProfileResDto;
import com.goojakgyo.goojakgyo.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class MemberQueryService {

  private final MemberRepository memberRepository;

  public MemberQueryService(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }

  public List<MemberListResDto> findMentors() {
    return findMembers(memberRepository.findByRole(Role.MENTOR));
  }

  public List<MemberListResDto> findMentees() {
    return findMembers(memberRepository.findByRole(Role.MENTEE));
  }

  private List<MemberListResDto> findMembers(List<Member> members) {
    List<MemberListResDto> memberListResDtos = new ArrayList<>();

    for (Member m : members) {
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

  public MemberProfileResDto getMemberProfile(Long memberId) {
    Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

    List<String> keywordNames = member.getMemberKeywords().stream().map(MemberKeyword::getKeyword).map(k -> k.getKeywordName()).collect(
        Collectors.toList());

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
}
