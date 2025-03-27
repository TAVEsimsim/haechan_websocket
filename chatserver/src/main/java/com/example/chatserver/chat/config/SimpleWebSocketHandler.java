//package com.example.chatserver.chat.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.*;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentSkipListSet;
//
//// /connet로 웹소켓 요청이 들어왔을 때 이를 처리할 클래스
//@Component // 스프링 빈으로 등록
//@Slf4j
//public class SimpleWebSocketHandler extends TextWebSocketHandler {
//    // TextWebSocketHandler를 상속받아서 사용
//    // TextWebSocketHandler는 TextMessage를 사용함
//    // TextMessage는 문자열을 담아서 전송하는 클래스
//    // 이 클래스는 문자열을 전송할 때 사용함
//
//    // 연결된 세션 관리 자료구조 : Thread-safe하게 관리해야 함 동시 연결 문제를 해결할 수 있는 ...
//    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        // 웹소켓 연결이 열리면 호출되는 메서드
//        // set을 통해서 사용자의 연결 정보를 등록할 것
//
//        sessions.add(session);
//        log.info("새로운 클라이언트가 연결되었습니다. 현재 연결 수 : {}", sessions.size());
//        log.info("연결된 클라이언트 정보 : {}", session);
//    }
//
//
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        /*
//                웹소켓으로 메세지가 들어왔을 때 호출되는 메서드
//            1) payload를 가져옴
//            2) payload를 모든 세션에 전송
//         */
//
//        // payload를 가져옴
//        String payload = message.getPayload();
//        log.info("received message : {}", payload);
//        for (WebSocketSession s : sessions) {
//            if (s.isOpen()){ // 해당 세션이 현재 메세지를 받을 수 있으면
//                s.sendMessage(new TextMessage(payload)); // 해당 세션에 메세지를 보냄
//            }
//        }
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        // 웹소켓 연결이 닫히면 호출되는 메서드
//        // set에서 사용자의 연결 정보를 제거할 것
//        sessions.remove(session);
//        log.info("클라이언트와 연결이 끊겼습니다. 현재 연결 수 : {}", sessions.size());
//    }
//
//}
