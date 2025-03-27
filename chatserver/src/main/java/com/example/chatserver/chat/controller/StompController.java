package com.example.chatserver.chat.controller;

import com.example.chatserver.chat.dto.ChatMessageReqDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@RequiredArgsConstructor
public class StompController {
    private final SimpMessageSendingOperations messageTemplate;
    /*
    방법 1)  MessageMapping(수신)과 SendTo(발신)을 한번에 처리

            @DestinationVariable을 통해서 roomId를 받아옴
            @DestinationVariable : @MessageMapping의 URL에서 변수를 받아올 때 사용
            클라이언트에게 특정 publish/roomId 형태로 메세지를 발행 시 MessageMapping 수신

    @MessageMapping("/{roomId}") //  해당 roomId에 메세지를 발행하며 구독 중인 클라이언트에게 메세지 전송
    @SendTo("/topic/{roomId}")
    public String sendMessage(@DestinationVariable Long roomId, String message){
        // /public/1로 오면 바로 SendTo를 통해서 /topic/1로 메세지를 보냄.
        // 즉 이 메세지가 메세지 브로커 역할을 함
        log.info("roomId : {}, message : {}", roomId, message);
        return message;
    }*/

    /*
    방법 2)
            MessageMapping만 활용하기
     */
    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageReqDto chatMessageReqDto){
        if (chatMessageReqDto == null || chatMessageReqDto.getMessage() == null) {
            log.warn("❌ 메시지가 null로 들어왔습니다!");
            return;
        }
        log.info("roomId : {},  message - {} : {}", roomId,chatMessageReqDto.getSenderEmail(), chatMessageReqDto.getMessage());
        // @SendTo 를 대신하는 중
        messageTemplate.convertAndSend("/topic/" + roomId, chatMessageReqDto);

    }
}
