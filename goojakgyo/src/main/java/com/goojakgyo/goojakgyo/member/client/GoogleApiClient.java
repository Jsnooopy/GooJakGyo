package com.goojakgyo.goojakgyo.member.client;

import com.goojakgyo.goojakgyo.member.domain.SocialType;
import com.goojakgyo.goojakgyo.member.dto.oauth.AccessTokenDto;
import com.goojakgyo.goojakgyo.member.dto.oauth.GoogleProfileDto;
import com.goojakgyo.goojakgyo.member.dto.profile.SocialProfile;
import com.goojakgyo.goojakgyo.member.service.oauth.GoogleService;
import org.springframework.stereotype.Component;

@Component("GOOGLE")
public class GoogleApiClient implements OAuthApiClient{

  private final GoogleService googleService;

  public GoogleApiClient(GoogleService googleService) {
    this.googleService = googleService;
  }

  @Override
  public AccessTokenDto requestAccessToken(String code) {
    return googleService.getAccessToken(code);
  }

  @Override
  public SocialProfile requestProfile(String accessToken) {
    GoogleProfileDto googleProfileDto = googleService.getGoogleProfile(accessToken);

    return SocialProfile.builder()
        .socialId(googleProfileDto.getSub())
        .name(googleProfileDto.getName())
        .email(googleProfileDto.getEmail())
        .socialType(SocialType.GOOGLE)
        .build();
  }
}
