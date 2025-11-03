package com.goojakgyo.goojakgyo.member.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberListResDto {
  private Long Id;
  private String name;
  private String email;
  private String univName;
  private String major;
}
