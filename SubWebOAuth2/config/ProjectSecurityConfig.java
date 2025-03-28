package com.kcj.SubWebOAuth2.config;


import com.kcj.SubWebOAuth2.Service.CustomOAuth2UserService;
import com.kcj.SubWebOAuth2.Service.CustomOidcUserService;
import com.kcj.SubWebOAuth2.constants.ApplicationConstants;
import com.kcj.SubWebOAuth2.exceptionhandling.CustomAccessDeniedHandler;
import com.kcj.SubWebOAuth2.filter.CsrfCookieFilter;
import com.kcj.SubWebOAuth2.filter.JWTGeneratorFilter;
import com.kcj.SubWebOAuth2.filter.JWTValidatorFilter;
import com.kcj.SubWebOAuth2.filter.RequestValidationBeforeFilter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Configuration

public class ProjectSecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;
    private final Environment env;
    public ProjectSecurityConfig(CustomOAuth2UserService customOAuth2UserService, CustomOidcUserService customOidcUserService,Environment env) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.customOidcUserService = customOidcUserService;
        this.env = env;
    }

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler = new CsrfTokenRequestAttributeHandler();
        http.securityContext(contextConfig -> contextConfig.requireExplicitSave(false)) // ?
                .sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //JWT 기반에서는 서버가 JWT를 기억할 필요가 없음(이론적으로)
                        .cors(corsConfig -> corsConfig.configurationSource(new CorsConfigurationSource() {
                            @Override
                            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                                CorsConfiguration config = new CorsConfiguration();
                                List<String> corsDomain = Arrays.asList("http://localhost:3000");
                                config.setAllowedOrigins(corsDomain);
                                config.setAllowedMethods(Collections.singletonList("*"));
                                config.setAllowCredentials(true);//
                                config.setAllowedHeaders(Collections.singletonList("*"));//?
                                config.setExposedHeaders(Arrays.asList("Authorization","XSRF-TOKEN", "Set-Cookie")); //헤더에 이를 넣어줘서 클라언트가 JWT을 받을 수 있게
                                config.setMaxAge(3600L); //3600밀리초
                                return config;
                            }
                        }))
                .formLogin().disable()
                .httpBasic(Customizer.withDefaults()) //postman basic auth의 기능을 사용하기 위해서 반드시 추가해야 함
                .csrf(csrfConfig -> csrfConfig.csrfTokenRequestHandler(csrfTokenRequestAttributeHandler)
                        .ignoringRequestMatchers("/register", "/login/oauth2/**",  // OAuth2 콜백 경로 CSRF 무시
                                "/oauth2/**")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())) //?
                .addFilterBefore(new RequestValidationBeforeFilter(), BasicAuthenticationFilter.class) //Basic 인증 필터 이전에 Basic 요청의 사전 검증
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(new JWTGeneratorFilter(), BasicAuthenticationFilter.class) //로그인 과정은 Basic Auth 이후에 토큰 생성
                .addFilterBefore(new JWTValidatorFilter(), BasicAuthenticationFilter.class)//로그인 이후에 API의 접근은 Basic Auth이전에 토큰을 먼저 검증
                .requiresChannel(rcc -> rcc.anyRequest().requiresInsecure()) // Only HTTP 프로덕션 환경시 request 루프 발생 가능
               // .requiresChannel(rcc -> rcc.anyRequest().requiresSecure()) //Only HTTPS
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/register","/error/**","/genre/**","/","/board","api/token","/login","/css/**","/js/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/board/content").permitAll()
                        .requestMatchers(HttpMethod.POST, "/board/content").authenticated()
                        .requestMatchers("/user","/logout","/comment").authenticated()
                        .requestMatchers("/profile/**","/getMessage").hasRole("USER")
                        .requestMatchers("/postImage","/post","/postMessage").hasAnyRole("USER","ADMIN")
                        .requestMatchers("/subculture").hasRole("ADMIN")
                        .anyRequest().authenticated()) //제대로 적용됨
                .oauth2Login(oauth2 -> oauth2.userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuth2UserService) //kakao, naver, github와 같은 기본적 OAuth2
                        .oidcUserService(customOidcUserService) //Google 과 같은 OpenID Connect 기반 OAuth2

                )
                        .successHandler(((request, response, authentication) -> {
                            String jwt = null;
                            if(authentication.getPrincipal() instanceof  CustomUserDetails userDetails) {
                                //여기서 CustomUserDetails 객체가 아닐 수도 있다..
                                jwt = generateJwt(userDetails);

                            } else if(authentication.getPrincipal() instanceof CustomOidcUser oidcUser) {
                                String email = oidcUser.getEmail();
                                jwt = generateJwtFromOidcUser(oidcUser);

                            } else {
                                throw new IllegalArgumentException("Invalid OAuth2 Process");
                            }

                            Cookie jwtCookie = new Cookie("JWT",jwt);
                            jwtCookie.setHttpOnly(false);
                            jwtCookie.setSecure(false);
                            jwtCookie.setPath("/");
                            jwtCookie.setMaxAge(3600);
                            response.addCookie(jwtCookie);
                            response.sendRedirect("http://localhost:3000");
                        })));

        http.exceptionHandling(ehc -> ehc.accessDeniedHandler(new CustomAccessDeniedHandler()));
        return http.build();
    }

    @Bean
    ClientRegistrationRepository clientRegistrationRepository(){
        ClientRegistration github = githubClientRegistration();
        ClientRegistration google = googleClientRegistration();
        ClientRegistration kakao = kakaoClientRegistration();
        ClientRegistration naver = naverClientRegistration();
        return new InMemoryClientRegistrationRepository(Arrays.asList(github,google,kakao,naver));
    }

    private String generateJwt(CustomUserDetails userDetails){
        String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY, ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        //HMAC-SHA 메시지의 무결성과 인증을 보장, 비밀 키를 사용해 암호화, 해시값 생성, 메시지 변조 여부 파악 가능
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = sdf.format(new Date());

        String jwt = Jwts.builder().issuer("SubWeb").subject("JWT") //subweb에서 발행한 JWT
                .claim("id", userDetails.getId())
                .claim("email",userDetails.getUsername())
                .claim("authorities",userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","))) //권한 String으로 받아서 ,로 나누기
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + 30000000)) //밀리초
                .signWith(secretKey).compact(); //서명을 해싱된 비밀키로 하는것 그렇기에 jwt 변조여부를 쉽게 알 수 있음
        return jwt;
    }

    private String generateJwtFromOidcUser(CustomOidcUser oidcUser){
        int id  = oidcUser.getAccountId();
        String email = oidcUser.getEmail();
        Collection<? extends GrantedAuthority> authorities = oidcUser.getAuthorities();
        String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY, ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
        SecretKey secretKey =Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        String jwt = Jwts.builder().issuer("SubWeb").subject("JWT") //subweb에서 발행한 JWT
                .claim("id",id)
                .claim("email",email)
                .claim("authorities",authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","))) //권한 String으로 받아서 ,로 나누기
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + 30000000)) //밀리초
                .signWith(secretKey).compact(); //서명을 해싱된 비밀키로 하는것 그렇기에 jwt 변조여부를 쉽게 알 수 있음
        return jwt;
    }
    private ClientRegistration githubClientRegistration(){
        return CommonOAuth2Provider.GITHUB.getBuilder("github")
                .clientId("***")
                .clientSecret("***")
                .redirectUri("http://localhost:8080/login/oauth2/code/github")
                .scope("read:user","user:email")
                .build();
    }

    private ClientRegistration googleClientRegistration(){
        return CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId("***")
                .clientSecret("***")
                .redirectUri("http://localhost:8080/login/oauth2/code/google") // 추가!!
                .scope("email","profile","openid")
                .build();
    } //구글은 깃허브와 다르게 email 정보를 얻기위해 따로 API를 구성하지 않아도 scope로 요청 정보만 더해주면 됨

    private ClientRegistration kakaoClientRegistration(){
        return ClientRegistration.withRegistrationId("kakao")
                .clientId("***")
                .clientSecret("***") //보통 카카오는 secret 사용안한다고 함..?
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/kakao")
                .scope("profile_nickname","account_email")
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .userInfoUri("https://kapi.kakao.com/v2/user/me")
                .userNameAttributeName("id")
                .clientName("kakao")
                .build();
    }

    private ClientRegistration naverClientRegistration(){
        return ClientRegistration.withRegistrationId("naver")
                .clientId("***")
                .clientSecret("***") //보통 카카오는 secret 사용안한다고 함..?
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/naver")
                .scope("name","email")
                .authorizationUri("https://nid.naver.com/oauth2.0/authorize")
                .tokenUri("https://nid.naver.com/oauth2.0/token")
                .userInfoUri("https://openapi.naver.com/v1/nid/me")
                .userNameAttributeName("response")
                .clientName("Naver")
                .build();
    }


    @Bean
    public CompromisedPasswordChecker compromisedPasswordChecker()
    {
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }//해당 사용자의 비밀번호가 유출된 적이 있는지를 검사하는 코드

}
