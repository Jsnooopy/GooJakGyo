package com.goojakgyo.goojakgyo.member.service;

import com.goojakgyo.goojakgyo.member.dto.AccessTokenDto;
import com.goojakgyo.goojakgyo.member.dto.NaverProfileDto;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.Token;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class NaverService {

  @Value("${oauth.naver.client-id}")
  private String naverClientId;

  @Value("${oauth.naver.client-secret}")
  private String naverClientSecret;

  @Value("${oauth.naver.redirect-uri}")
  private String naverRedirectUri;

  public AccessTokenDto getAccessToken(String code) {
    // 인가 코드, clientId, client_secret, redirect_uri, grant_type

    // Spring6부터 RestTemplate 비추천(Future Deprecate)이기 때문에 RestClient 사용
    RestClient restClient = RestClient.create();

    // MultiValueMap을 통해 자동으로 form-data 형식으로 body 조립 가능
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("code", code);
    params.add("client_id", naverClientId);
    params.add("client_secret", naverClientSecret);
    params.add("redirect_uri", naverRedirectUri);
    params.add("grant_type", "authorization_code");

    ResponseEntity<AccessTokenDto> response = restClient.post()
        .uri("https://nid.naver.com/oauth2.0/token")
        .header("Content-Type", "application/x-www-form-urlencoded")
        .body(params)
        .retrieve()
        .toEntity(AccessTokenDto.class);

    log.info("Access Token JSON : {}", response.getBody());
    return response.getBody();
  }

  public NaverProfileDto getNaverProfile(String token) {
    RestClient restClient = RestClient.create();

    ResponseEntity<NaverProfileDto> response = restClient.get()
        .uri("https://openapi.naver.com/v1/nid/me")
        .header("Authorization", "Bearer " + token)
        .retrieve()
        .toEntity(NaverProfileDto.class);

    log.info("Profile JSON : {}", response.getBody());
    return response.getBody();
  }
}
