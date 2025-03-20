package com.example.chatserver.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.Signature;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final String secretkey;
    private final int expiration;
    private Key SECRET_KEY;

    public JwtTokenProvider(@Value("${jwt.secretKey}")String secretkey,  @Value("${jwt.expiration}")int expiration) {
        this.secretkey = secretkey;
        this.expiration = expiration;
        this.SECRET_KEY = new SecretKeySpec(
                java.util.Base64.getDecoder().decode(secretkey),  // secretkey를 디코딩해서 byte로 변환
                SignatureAlgorithm.HS512.getJcaName()); // HS512를 이용하여 암호화시킴
    }

    public String createToken(String email, String role){
        // 토큰을 생성하는 부분

        // claims는 payload에 해당하는 부분
        Claims claims = Jwts.claims().setSubject(email);
        // payload에 해당하는 부분에 email, role을 넣어줌
        claims.put("role", role);
        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime()+expiration*60*1000L)) // 만료 일자 설정. 현재 시간에 토큰 만료 시간을 더해줌
                .signWith(SECRET_KEY) // 고유 ...? 키
                .compact();
        return token;
    }
}