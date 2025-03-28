package com.kcj.SubWebOAuth2.filter;

import com.kcj.SubWebOAuth2.config.CustomUserDetails;
import com.kcj.SubWebOAuth2.constants.ApplicationConstants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JWTGeneratorFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal (HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws IOException, ServletException{
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(null != authentication && authentication.getPrincipal() instanceof CustomUserDetails){
            Environment env = getEnvironment(); //환경변수 (core.env Generic Filter Bean)
            if(null != env){
                String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY, ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
                SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); 
                //HMAC-SHA 메시지의 무결성과 인증을 보장, 비밀 키를 사용해 암호화, 해시값 생성, 메시지 변조 여부 파악 가능
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal(); //안전하게 캐스팅
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = sdf.format(new Date());

                String jwt = Jwts.builder().issuer("SubWeb").subject("JWT") //subweb에서 발행한 JWT
                        .claim("id", userDetails.getId())
                        .claim("email",authentication.getName())
                        .claim("authorities",authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","))) //권한 String으로 받아서 ,로 나누기
                        .issuedAt(new Date())
                        .expiration(new Date(new Date().getTime() + 30000000)) //밀리초
                        .signWith(secretKey).compact(); //서명을 해싱된 비밀키로 하는것 그렇기에 jwt 변조여부를 쉽게 알 수 있음
                response.setHeader(ApplicationConstants.JWT_HEADER,jwt); //응답에 jwt를 헤더로
            }
        }
        filterChain.doFilter(request,response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException{
        return !(request.getServletPath().equals("/user")); //true면 필터 동작안함
        //User API로 접속할 때, oauth2 login으로 접속할 때 JWT 생성 필터가 동작하도록 함
    }
}
