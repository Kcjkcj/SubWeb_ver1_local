package com.kcj.SubWebOAuth2.filter;

import com.kcj.SubWebOAuth2.constants.ApplicationConstants;
import com.kcj.SubWebOAuth2.config.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class JWTValidatorFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal (HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        String jwt = request.getHeader(ApplicationConstants.JWT_HEADER);
        if(null != jwt && jwt.startsWith("Bearer ")){ //Bearer  접두사를 붙이는게 원칙
            try {
                Environment env = getEnvironment(); //환경변수 (core.env Generic Filter Bean)
                if (null != env) {
                    jwt = jwt.substring(7); //Bearer 접두사 제거
                    String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY, ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
                    SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); //SHA
                    if (secretKey != null) {
                        Claims claims = Jwts.parser().verifyWith(secretKey) 
                                .build().parseSignedClaims(jwt).getPayload(); //생성기에서 만들때 서명된 비밀키를 사용
                        //build 과정에서 토큰의 변조가 검증됨. parser로 (헤더, 페이로드, 서명)으로 분리되며, 클레임 정보도 메타데이터를 통해 얻을 수 있음
                        int id = Integer.parseInt(claims.get("id").toString()); //jwt에 id를 받아야지
                        String username = String.valueOf(claims.get("email"));
                        String authorities = String.valueOf(claims.get("authorities"));

                        List<GrantedAuthority> grantedAuthorityList = AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);
                        CustomUserDetails userDetails = new CustomUserDetails(id,username,"",grantedAuthorityList);
                        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, grantedAuthorityList);
                        //여기서 인증 객체를 userDetails으로 해야 Principal이 CustomUserDetails 객체로 저장이 됨.. 그래야만 JWT 생성기에서도 Principal을 CustomUserDetails로 인식가능
                        SecurityContextHolder.getContext().setAuthentication(authentication); //인증 객체에 id정보를 추가하면 컨트롤러에서 id값을 인식하고 통과/차단을 할 수 있음
                    }
                }
            } catch (Exception exception){
                throw new BadCredentialsException("타당하지 않은 토큰을 받았습니다.");
            }
        }
        filterChain.doFilter(request,response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException{
        return (request.getServletPath().equals("/user") //true면 필터 동작안함
        //User API로 접속할 때만 JWT 검증 필터가 동작하지 않도록. 아직 로그인을 안했으니 토큰이 있을리 없기 때문에
        || request.getServletPath().startsWith("/login/oauth2"));
    }
}
