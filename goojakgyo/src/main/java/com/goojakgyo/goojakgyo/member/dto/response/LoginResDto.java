package com.goojakgyo.goojakgyo.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResDto {
  private Long id;
  private String name;
  private String accessToken;
  private String refreshToken;
}
