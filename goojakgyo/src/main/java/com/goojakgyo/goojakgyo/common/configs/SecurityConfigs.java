package com.goojakgyo.goojakgyo.common.configs;

import com.goojakgyo.goojakgyo.common.earth.JwtAuthFilter;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfigs {

  private final JwtAuthFilter jwtAuthFilter;

  public SecurityConfigs(JwtAuthFilter jwtAuthFilter) {
    this.jwtAuthFilter = jwtAuthFilter;
  }

  @Bean
  public SecurityFilterChain myFilter(HttpSecurity httpSecurity) throws Exception{
    return httpSecurity
        .cors(cors->cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)//csrf 비활성화
        .httpBasic(AbstractHttpConfigurer::disable) //HTTP Basic 비활성화
        .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //세션방식을 사용하지 않겠다라는 의미
        // 특정 url패턴에 대해서는 Authentication객체 요구하지 않음.(인증처리 제외)
        .authorizeHttpRequests(a -> a.requestMatchers("/member/create", "/member/doLogin",
            "/member/google/doLogin", "/member/kakao/doLogin", "/member/naver/doLogin", "/connect/**").permitAll().anyRequest().authenticated())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(){
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    configuration.setAllowedMethods(Arrays.asList("*")); // 모든 HTTP메서드 허용
    configuration.setAllowedHeaders(Arrays.asList("*")); // 모든 헤더값 허용
    configuration.setAllowCredentials(true); // 자격증명 허용

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration); // 모든 url에 패턴에 대해 cors 허용 설정

    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder(){
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }
}
