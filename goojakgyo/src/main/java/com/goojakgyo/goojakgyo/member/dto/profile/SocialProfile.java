package com.goojakgyo.goojakgyo.member.dto.profile;

import com.goojakgyo.goojakgyo.member.domain.SocialType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SocialProfile {
  private String socialId;
  private String name;
  private String email;
  private SocialType socialType;
}
