package com.goojakgyo.goojakgyo.member.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
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

  @Builder.Default
  private String profileImageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSmYmaWrQ2kwplFb1FN1a07DmEKCAiIA4j31TfkvVr4STcOqQP7M-hITB3gPuckOSdRb8I&usqp=CAU";

  public void updateProfileImageUrl(String profileImageUrl) {
    this.profileImageUrl = profileImageUrl;
  }
  
  // 학교 정보
  @Column(nullable = false)
  private String univName;
  
  @Column(nullable = false)
  private String major;
  
  @Column(nullable = false, unique = true)
  private String studentId;
  
  // Role
  @Enumerated(EnumType.STRING)
  private Role role;

  // Social Login 정보
  @Enumerated(EnumType.STRING)
  private SocialType socialType;

  private String socialId;

  // Keword 정보
  @Builder.Default
  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<MemberKeyword> memberKeywords = new ArrayList<>();

  public void addMemberKeyword(MemberKeyword memberKeyword) {
    memberKeywords.add(memberKeyword);
  }
}
