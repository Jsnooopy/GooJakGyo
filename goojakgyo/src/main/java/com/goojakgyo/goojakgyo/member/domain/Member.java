package com.goojakgyo.goojakgyo.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Member {
  
  // 회원 정보
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String email;

  private String password;
  
  // 학교 정보
  @Column(nullable = false)
  private String univName;
  
  @Column(nullable = false)
  private String major;
  
  @Column(nullable = false, unique = true)
  private String studentId;
  
  // Role
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Role role = Role.USER;

  // Social Login 정보
  @Enumerated(EnumType.STRING)
  private SocialType socialType;

  private String socialId;
}
