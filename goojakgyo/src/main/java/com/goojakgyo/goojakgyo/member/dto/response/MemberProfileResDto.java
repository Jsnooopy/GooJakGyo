package com.goojakgyo.goojakgyo.member.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberProfileResDto {
  private Long id;
  private String name;
  private String email;

  private String univName;
  private String major;
  private String studentId;

  private String profileImageUrl;
  private List<String> keywords;
}
