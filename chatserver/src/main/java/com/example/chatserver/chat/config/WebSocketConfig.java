//package com.example.chatserver.chat.config;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.socket.config.annotation.EnableWebSocket;
//import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
//import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
//
//
//@Configuration
//@EnableWebSocket // 웹소켓 활성화 -> Build.gradle에 의존성 추가로 인해서 사용 가능  함
//@RequiredArgsConstructor
//public class WebSocketConfig implements WebSocketConfigurer {
//    private final SimpleWebSocketHandler simpleWebSocketHandler;
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        // 웹소켓 소스코드를 처리하는 핸들러
//
//        /*
//            스프링이 시작되면 웹소켓 소스 코드를 여기서 처리를 하는 것
//         */
//
//        // /connect URL로 웹소켓 연결이 들어오면, 등록한 핸들러 클래스가 그걸 처리 함 -> SimpleWebSocketHandler 싱글톤 객체로 주입을 받은 걸 씀
//        registry.addHandler(simpleWebSocketHandler, "/connect")
//                .setAllowedOrigins("*"); // CORS 허용 : SecurityConfigs에서의 CORS 예외는 HTTP 요청에 대한 예외 -> 웹소켓은 따로 설정해줘야 함
//    }
//}
