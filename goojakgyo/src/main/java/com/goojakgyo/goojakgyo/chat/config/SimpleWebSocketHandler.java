//// 웹소켓 전용 Handler이므로 충돌 방지를 위해 STOMP 사용할 땐 전체 주석 처리
//
//package com.goojakgyo.goojakgyo.chat.config;
//
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//
//// connect로 웹소켓 연결 요청이 들어왔을 때 연결을 처리하는 핸들러
//@Component // 싱글톤 객체
//public class SimpleWebSocketHandler extends TextWebSocketHandler {
//    // 연결된 세션 정보 저장
//    // 그냥 HashSet<>은 Thread-Safe하지 않기 때문에 ConcurrentHashMap 사용
//    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
//
//    // 연결되면 set 자료 구조에 사용자의 연결 정보 등록
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        sessions.add(session);
//        System.out.println("Connected : " + session.getId());
//    }
//
//    // 사용자에게 메시지를 보내주는 메서드
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        String payload = message.getPayload();
//        System.out.println("received message : " + payload);
//
//        for(WebSocketSession s : sessions) {
//            if(s.isOpen()) {
//                s.sendMessage(new TextMessage(payload));
//            }
//        }
//    }
//
//    // 연결 끊기면 세션을 메모리에서 삭제
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        sessions.remove(session);
//        System.out.println("disconnected!!");
//    }
//}
