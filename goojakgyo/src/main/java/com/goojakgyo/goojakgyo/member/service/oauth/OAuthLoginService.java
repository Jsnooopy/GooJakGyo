package com.goojakgyo.goojakgyo.member.service.oauth;

import com.goojakgyo.goojakgyo.member.client.OAuthApiClient;
import com.goojakgyo.goojakgyo.member.domain.Member;
import com.goojakgyo.goojakgyo.member.domain.SocialType;
import com.goojakgyo.goojakgyo.member.dto.profile.SocialProfile;
import com.goojakgyo.goojakgyo.member.dto.response.LoginOrProfileResDto;
import com.goojakgyo.goojakgyo.member.dto.response.LoginResDto;
import com.goojakgyo.goojakgyo.member.repository.MemberRepository;
import com.goojakgyo.goojakgyo.member.service.auth.AuthService;
import jakarta.transaction.Transactional;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class OAuthLoginService {

  // "GOOGLE", "KAKAO", "NAVER"
  private final Map<String, OAuthApiClient> clients;
  private final MemberRepository memberRepository;
  private final AuthService authService;

  public OAuthLoginService(Map<String, OAuthApiClient> clients, MemberRepository memberRepository,
      AuthService authService) {
    this.clients = clients;
    this.memberRepository = memberRepository;
    this.authService = authService;
  }

  public LoginOrProfileResDto loginOrCheck(String code, SocialType socialType) {
    OAuthApiClient client = clients.get(socialType.name());
    var token = client.requestAccessToken(code);
    SocialProfile profile = client.requestProfile(token.getAccess_token());

    Member member = memberRepository.findBySocialId(profile.getSocialId()).orElse(null);

    if (member == null) {
      return LoginOrProfileResDto.builder()
          .isNewUser(true)
          .socialProfile(profile)
          .build();
    }

    LoginResDto loginResDto = authService.issueTokens(member);
    return LoginOrProfileResDto.builder()
        .isNewUser(false)
        .loginResDto(loginResDto)
        .build();
  }
}
