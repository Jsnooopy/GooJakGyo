package com.goojakgyo.goojakgyo.member.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberListReqDto {
  private Long Id;
  private String name;
  private String email;
}
