package com.goojakgyo.goojakgyo.member.dto.request;

import com.goojakgyo.goojakgyo.member.domain.Role;
import com.goojakgyo.goojakgyo.member.domain.SocialType;
import java.util.List;
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
  private Role role;

  private List<Long> keywordIds;
}
