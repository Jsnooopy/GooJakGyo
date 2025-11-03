package com.goojakgyo.goojakgyo.member.client;

import com.goojakgyo.goojakgyo.member.dto.oauth.AccessTokenDto;
import com.goojakgyo.goojakgyo.member.dto.profile.SocialProfile;

public interface OAuthApiClient {

  AccessTokenDto requestAccessToken(String code);

  SocialProfile requestProfile(String accessToken);
}
