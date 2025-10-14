package com.goojakgyo.goojakgyo.member.service;

import static org.junit.jupiter.api.Assertions.*;

import com.goojakgyo.goojakgyo.member.domain.Member;
import com.goojakgyo.goojakgyo.member.dto.MemberSaveReqDto;
import com.goojakgyo.goojakgyo.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
class MemberServiceTest {
  @Autowired
  private MemberService memberService;

  @Autowired
  private MemberRepository memberRepository;

  @Test
  @DisplayName("회원가입이 정상적으로 성공해야 한다.")
  void createMember_Success() {
    // Given
    MemberSaveReqDto memberSaveReqDto = new MemberSaveReqDto("testuser", "test@example.com",
        "1234");

    // When
    Member newMember = memberService.create(memberSaveReqDto);

    // Then
    assertNotNull(newMember.getId());

    Member foundMember = memberRepository.findByEmail("test@example.com").get();
    assertEquals("testuser", foundMember.getName());
    assertEquals("test@example.com", foundMember.getEmail());
    assertEquals("1234", foundMember.getPassword());
  }

  @Test
  @DisplayName("중복된 이메일로 가입을 시도하면 예외가 발생해야 한다.")
  void createMember_Fail_DuplicateEmail() {
    // Given (주어진 상황)
    // 먼저 동일한 이메일을 가진 회원을 DB에 저장해둔다.
    Member existingMember = Member.builder()
        .name("기존유저")
        .email("test@example.com")
        .password("password123")
        .build();
    memberRepository.save(existingMember);

    // 중복된 이메일로 가입을 시도할 정보
    MemberSaveReqDto reqDto = new MemberSaveReqDto("새로운유저", "test@example.com", "password456");

    // When & Then (무엇을 할 때 & 결과는 이래야 한다)
    // assertThrows: 특정 예외가 발생하는지 검증하는 메소드
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      memberService.create(reqDto); // 이 코드를 실행했을 때
    });

    // 발생한 예외의 메시지가 우리가 예상한 메시지와 일치하는지 확인
    assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
  }
}