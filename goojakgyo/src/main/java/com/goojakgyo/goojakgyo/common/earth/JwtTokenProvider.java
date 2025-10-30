package com.goojakgyo.goojakgyo.common.earth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.util.Date;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private final String secretKey;
  private final int accessTokenExpiration;
  private final int refreshTokenExpiration;
  private final Key SECRET_KEY;

  public JwtTokenProvider(@Value("${jwt.secretKey}") String secretKey, @Value("${jwt.access-expiration}") int accessTokenExpiration,
      @Value("${jwt.refresh-expiration}") int refreshTokenExpiration) {
    this.secretKey = secretKey;
    this.accessTokenExpiration = accessTokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;
    this.SECRET_KEY = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKey), SignatureAlgorithm.HS512.getJcaName());
  }

  // 토큰 생성 시 이름도 함께 넘기도록
  public String createToken(String email, String name, String role, int expiration) {
    Claims claims = Jwts.claims().setSubject(email);
    if (role!= null) claims.put("role", role);
    if (name != null) claims.put("name", name);
    Date now = new Date();

    String token = Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + expiration * 60 * 1000L))
        .signWith(SECRET_KEY)
        .compact();

    return token;
  }

  public String createAccessToken(String email, String name, String role) {
    return createToken(email, name, role, accessTokenExpiration);
  }

  public String createRefreshToken(String email, String name) {
    return createToken(email, name, null, refreshTokenExpiration);
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public String getEmailFromToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(SECRET_KEY)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

}
