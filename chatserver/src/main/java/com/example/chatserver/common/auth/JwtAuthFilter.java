package com.example.chatserver.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthFilter extends GenericFilter {
    // 시크릿 키 가져 옴
    @Value("${jwt.secretKey}")
    private String secretKey;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        /*
               매개변수 설명
               1. request : token이 들어가 있음
               2. response : 토큰에 문제가 있으면 response에 반환
               3. chain : 토큰에 문제가 있으면 돌아갈 때 사용
         */
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        String token = httpServletRequest.getHeader("Authorization");
        try {

            if (token != null) { // 토큰이 있는 경우에만 검증할 것
                if (!token.substring(0, 7).equals("Bearer ")) { // 앞에 이거 붙어 있는지 체크. 관례적으로 Bearer로 시작한다고 함
                    throw new AuthenticationServiceException("Bearer 형식이 ㄴㄴ");
                }
                String jwtToken = token.substring(7); //Bearer 떄내고 토큰의 원본만 꺼냄

                // 우리 서비스에서 발급한 토큰인지 체크하는 방법 : 똑같이 암호화해서 시그니처 부분이 같은지 확인하기

                //토큰 검증 및 claims (payload) 추출
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(secretKey)
                        .build()
                        .parseClaimsJws(jwtToken)
                        .getBody();

                //authentication 객체 생성
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + claims.get("role").toString()));
                UserDetails userDetails = new User(claims.getSubject(), "", authorities);
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication); //계층 구조
            }
            chain.doFilter(request, response);
        }catch (Exception e) {
            e.printStackTrace();
            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().write("invalid token");
        }
    }
}