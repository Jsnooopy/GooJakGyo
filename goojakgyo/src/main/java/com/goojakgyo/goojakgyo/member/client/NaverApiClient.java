package com.goojakgyo.goojakgyo.member.client;

import com.goojakgyo.goojakgyo.member.domain.SocialType;
import com.goojakgyo.goojakgyo.member.dto.oauth.AccessTokenDto;
import com.goojakgyo.goojakgyo.member.dto.oauth.NaverProfileDto;
import com.goojakgyo.goojakgyo.member.dto.profile.SocialProfile;
import com.goojakgyo.goojakgyo.member.service.oauth.NaverService;
import org.springframework.stereotype.Component;

@Component("NAVER")
public class NaverApiClient implements OAuthApiClient{

  private final NaverService naverService;

  public NaverApiClient(NaverService naverService) {
    this.naverService = naverService;
  }


  @Override
  public AccessTokenDto requestAccessToken(String code) {
    return naverService.getAccessToken(code);
  }

  @Override
  public SocialProfile requestProfile(String accessToken) {
    NaverProfileDto naverProfileDto = naverService.getNaverProfile(accessToken);

    return SocialProfile.builder()
        .socialId(naverProfileDto.getResponse().getId())
        .name(naverProfileDto.getResponse().getName())
        .email(naverProfileDto.getResponse().getEmail())
        .socialType(SocialType.NAVER)
        .build();
  }
}
