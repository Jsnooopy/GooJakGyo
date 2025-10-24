package com.goojakgyo.goojakgyo.member.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "member_keyword")
public class MemberKeyword {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "keyword_id")
  private Keyword keyword;

  private MemberKeyword(Member member, Keyword keyword) {
    this.member = member;
    this.keyword = keyword;
  }

  public static MemberKeyword of(Member member, Keyword keyword) {
    return MemberKeyword.builder()
        .member(member)
        .keyword(keyword)
        .build();
  }

}
