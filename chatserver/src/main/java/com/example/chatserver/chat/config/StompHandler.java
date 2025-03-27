package com.example.chatserver.chat.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StompHandler implements ChannelInterceptor {
    /*
            이전에는 뭐 연결되면 Set에 Session 넣어주고 연결 끊기면
            Set에서 Session 제거해주는 것을 했었음
            근데 이런 걸 STOMP에서 알아서 해줌 ㅇㅇ
            => 여기서는 인증하는 작업을 할 것 (토큰을 통해서)
     */
    @Value("${jwt.secretKey}")
    private String secretKey;

    // connect, subscribe, disconnect 하기 전에 이 메소드를 거침
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // 사용자 요청은 message 안에 담겨 있으니 이걸 꺼낼 것
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);


        if (StompCommand.CONNECT == accessor.getCommand()) {
            // CONNECT 요청이 들어왔을 때
            // 여기서 토큰 검증을 해야 함
            log.info("Connect 요청 시 토큰 유효성 검증 시작");
            // "Authorization"로 담겨진 토큰 꺼내기
            String bearerToken = accessor.getFirstNativeHeader("Authorization");

            // "Bearer " 이 부분을 제거해야 함
            String token = bearerToken.substring(7);

            // 토큰 검증
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            log.info("토큰 검증 완료!!");
        }
        return ChannelInterceptor.super.preSend(message, channel);
    }
}
