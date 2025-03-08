package com.kcj.SubWeb.config;


import com.kcj.SubWeb.exceptionhandling.CustomAccessDeniedHandler;
import com.kcj.SubWeb.exceptionhandling.CustomAuthenticationEntryPoint;
import com.kcj.SubWeb.filter.CsrfCookieFilter;
import com.kcj.SubWeb.filter.JWTGeneratorFilter;
import com.kcj.SubWeb.filter.JWTValidatorFilter;
import com.kcj.SubWeb.filter.RequestValidationBeforeFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.session.DefaultCookieSerializerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class ProjectSecurityConfig {
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler = new CsrfTokenRequestAttributeHandler();
        http.securityContext(contextConfig -> contextConfig.requireExplicitSave(false)) // ?
                .sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                        .cors(corsConfig -> corsConfig.configurationSource(new CorsConfigurationSource() {
                            @Override
                            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                                CorsConfiguration config = new CorsConfiguration();
                                List<String> corsDomain = Arrays.asList("http://localhost:3000");
                                config.setAllowedOrigins(corsDomain);
                                config.setAllowedMethods(Collections.singletonList("*"));
                                config.setAllowCredentials(true);//
                                config.setAllowedHeaders(Collections.singletonList("*"));//?
                                config.setExposedHeaders(Arrays.asList("Authorization")); //헤더에 이를 넣어줘서 클라언트가 JWT을 받을 수 있게
                                config.setMaxAge(3600L); //3600밀리초
                                return config;
                            }
                        }))
                .csrf(csrfConfig -> csrfConfig.csrfTokenRequestHandler(csrfTokenRequestAttributeHandler)
                        .ignoringRequestMatchers("/register")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())) //?
                .addFilterBefore(new RequestValidationBeforeFilter(), BasicAuthenticationFilter.class) //Basic 인증 필터 이전에 Basic 요청의 사전 검증
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(new JWTGeneratorFilter(), BasicAuthenticationFilter.class) //로그인 과정은 Basic Auth 이후에 토큰 생성
                .addFilterBefore(new JWTValidatorFilter(), BasicAuthenticationFilter.class)//로그인 이후에 API의 접근은 Basic Auth이전에 토큰을 먼저 검증
                .requiresChannel(rcc -> rcc.anyRequest().requiresInsecure()) // Only HTTP
               // .requiresChannel(rcc -> rcc.anyRequest().requiresSecure()) //Only HTTPS
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/register","/error/**","/genre/**","/","/board").permitAll()
                        .requestMatchers(HttpMethod.GET, "/board/content").permitAll()
                        .requestMatchers(HttpMethod.POST, "/board/content").authenticated()
                        .requestMatchers("/user","/logout","/comment").authenticated()
                        .requestMatchers("/profile/**","/getMessage").hasRole("USER")
                        .requestMatchers("/postImage","/post","/postMessage").hasAnyRole("USER","ADMIN")
                        .requestMatchers("/subculture").hasRole("ADMIN")
                        .anyRequest().hasRole("ADMIN")); //제대로 적용됨

        http.httpBasic(Customizer.withDefaults()); //postman basic auth의 기능을 사용하기 위해서 반드시 추가해야 함
        http.exceptionHandling(ehc -> ehc.accessDeniedHandler(new CustomAccessDeniedHandler()));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() //설정 클래스에서 bean으로 설정해줘야 PasswordEncoder 사용가능
    {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
    
    @Bean
    public CompromisedPasswordChecker compromisedPasswordChecker()
    {
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }//해당 사용자의 비밀번호가 유출된 적이 있는지를 검사하는 코드

}
