package com.goojakgyo.goojakgyo.member.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.goojakgyo.goojakgyo.member.dto.profile.SocialProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginOrProfileResDto {
  private boolean isNewUser;
  private LoginResDto loginResDto;
  private SocialProfile socialProfile;
}
