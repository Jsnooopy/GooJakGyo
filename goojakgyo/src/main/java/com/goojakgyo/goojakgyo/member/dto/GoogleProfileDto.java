package com.goojakgyo.goojakgyo.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 없는 필드는 자동 무시
public class GoogleProfileDto {
  private String sub;
  private String email;
  private String name;
  private String picture;
}
