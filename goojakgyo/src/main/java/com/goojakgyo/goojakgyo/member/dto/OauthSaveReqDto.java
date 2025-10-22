package com.goojakgyo.goojakgyo.member.dto;

import com.goojakgyo.goojakgyo.member.domain.SocialType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OauthSaveReqDto {
  private String name;
  private String email;
  private String socialId;
  private SocialType socialType;

  private String univName;
  private String major;
  private String studentId;
}
