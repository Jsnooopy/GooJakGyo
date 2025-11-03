package com.goojakgyo.goojakgyo.member.client;

import com.goojakgyo.goojakgyo.member.domain.SocialType;
import com.goojakgyo.goojakgyo.member.dto.oauth.AccessTokenDto;
import com.goojakgyo.goojakgyo.member.dto.oauth.KakaoProfileDto;
import com.goojakgyo.goojakgyo.member.dto.profile.SocialProfile;
import com.goojakgyo.goojakgyo.member.service.oauth.KakaoService;
import org.springframework.stereotype.Component;

@Component("KAKAO")
public class KakaoApiClient implements OAuthApiClient{

  private final KakaoService kakaoService;

  public KakaoApiClient(KakaoService kakaoService) {
    this.kakaoService = kakaoService;
  }


  @Override
  public AccessTokenDto requestAccessToken(String code) {
    return kakaoService.getAccessToken(code);
  }

  @Override
  public SocialProfile requestProfile(String accessToken) {
    KakaoProfileDto kakaoProfileDto = kakaoService.getKakaoProfile(accessToken);

    return SocialProfile.builder()
        .socialId(kakaoProfileDto.getId())
        .name(kakaoProfileDto.getKakao_account().getProfile().getNickname())
        .email(kakaoProfileDto.getKakao_account().getEmail())
        .socialType(SocialType.KAKAO)
        .build();
  }
}
