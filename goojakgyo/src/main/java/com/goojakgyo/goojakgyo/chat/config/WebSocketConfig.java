//// 웹소켓 전용 Config이므로 충돌 방지를 위해 STOMP 사용할 땐 전체 주석 처리
//
//package com.goojakgyo.goojakgyo.chat.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.socket.config.annotation.EnableWebSocket;
//import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
//import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
//
//// WebSocket 최초 연결을 위해 구성하는 Config 파일
//@Configuration
//@EnableWebSocket
//public class WebSocketConfig implements WebSocketConfigurer {
//    private final SimpleWebSocketHandler simpleWebSocketHandler;
//
//    public WebSocketConfig(SimpleWebSocketHandler simpleWebSocketHandler) {
//        this.simpleWebSocketHandler = simpleWebSocketHandler;
//    }
//
//    // WebSocket 코드를 처리할 Handler 등록
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        System.out.println("최초 WebSocket 연결을 위한 등록 Handler");
//
//        // /connect url로 websocket 연결 요청 들어오면, SimpleWebSocketHandler가 처리
//        registry.addHandler(simpleWebSocketHandler, "/connect")
//                // SecurityConfig에서 websocket 프로토콜에 대한 요청은 별도의 cors 설정되어 있음
//                .setAllowedOrigins("http://localhost:3000");
//    }
//}
