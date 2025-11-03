package com.goojakgyo.goojakgyo.member.service.auth;

import com.goojakgyo.goojakgyo.common.earth.JwtTokenProvider;
import com.goojakgyo.goojakgyo.member.domain.Member;
import com.goojakgyo.goojakgyo.member.domain.RefreshToken;
import com.goojakgyo.goojakgyo.member.dto.request.MemberLoginReqDto;
import com.goojakgyo.goojakgyo.member.dto.response.LoginResDto;
import com.goojakgyo.goojakgyo.member.repository.MemberRepository;
import com.goojakgyo.goojakgyo.member.repository.RefreshTokenRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AuthService {

  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final MemberRepository memberRepository;
  private final RefreshTokenRepository refreshTokenRepository;

  public AuthService(PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, MemberRepository memberRepository,
      RefreshTokenRepository refreshTokenRepository) {
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenProvider = jwtTokenProvider;
    this.memberRepository = memberRepository;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  // login email, password 검증
  public LoginResDto login(MemberLoginReqDto memberLoginReqDto) {
    Member member = memberRepository.findByEmail(memberLoginReqDto.getEmail())
        .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 이메일 입니다."));

    if (!passwordEncoder.matches(memberLoginReqDto.getPassword(), member.getPassword())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    return issueTokens(member);
  }

  // 검증 후 AccessToken, RefreshToken 발급
  public LoginResDto issueTokens(Member member) {
    // access token & refresh token 발급
    String accessToken = jwtTokenProvider.createAccessToken(member.getEmail(), member.getRole().toString());
    String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail());

    // Redis 저장
    RefreshToken redisRefreshToken = RefreshToken.builder()
        .memberId(member.getId())
        .token(refreshToken)
        .expiration(60 * 60 * 24 * 3 * 1000L)
        .build();

    refreshTokenRepository.save(redisRefreshToken);

    return LoginResDto.builder()
        .id(member.getId())
        .name(member.getName())
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  // Refresh Token 유효성 검증 후 Access Token, Refresh Token 재발급
  public LoginResDto reissueTokens(String refreshToken) {
    // 유효성 검증
    if (!jwtTokenProvider.validateToken(refreshToken)) throw new IllegalArgumentException("Expired / Invalid Refresh Token");

    // email 추출 및 회원 조회
    String email = jwtTokenProvider.getEmailFromToken(refreshToken);
    Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));

    // Redis에 저장되어 있는 Token과 일치하는지 검증
    RefreshToken stored = refreshTokenRepository.findById(member.getId()).orElse(null);
    if (stored == null || !stored.getToken().equals(refreshToken)) throw new IllegalArgumentException("Invalid Refresh Token");

    // 새 Access Token 및 Refresh Token 발급 및 저장
    String newAccessToken = jwtTokenProvider.createAccessToken(email, member.getRole().toString());
    String newRefreshToken = jwtTokenProvider.createRefreshToken(email);

    stored.setToken(newRefreshToken);
    stored.setExpiration(60 * 60 * 24 * 3 * 1000L);
    refreshTokenRepository.save(stored);

    return LoginResDto.builder()
        .id(member.getId())
        .name(member.getName())
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .build();
  }

  // logout시 Redis에 저장된 Refresh Token 삭제
  public void logout(Long id) {
    refreshTokenRepository.deleteById(id);
  }

}
