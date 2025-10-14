package com.goojakgyo.goojakgyo.member.dto;

// 회원가입시 요청되는 dto

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberSaveReqDto {
  private String name;
  private String email;
  private String password;
}
