package com.goojakgyo.goojakgyo.member.dto;

import com.goojakgyo.goojakgyo.member.domain.Role;
import java.util.List;
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

  private String univName;
  private String major;
  private String studentId;
  private Role role;

  private List<Long> keywordIds;
}
