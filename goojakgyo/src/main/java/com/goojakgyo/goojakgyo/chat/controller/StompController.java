package com.goojakgyo.goojakgyo.chat.controller;

import com.goojakgyo.goojakgyo.chat.dto.ChatMessageDto;
import com.goojakgyo.goojakgyo.chat.service.ChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
public class StompController {
    // 방법2에서 메시지 전달을 위한 객체
    private final SimpMessageSendingOperations messageTemplate;

    private final ChatService chatService;

    public StompController(SimpMessageSendingOperations messageTemplate, ChatService chatService) {
        this.messageTemplate = messageTemplate;
        this.chatService = chatService;
    }

//    // 방법 1. MessageMapping(수신)과 SendTo(topic에 메시지 전달) 한꺼번에 처리
//    // 메시지 브로커 역할
//    @MessageMapping("/{roomId}") // 클라이언트에서 특정 publish/roomId 형태로 메시지 발행시 MessageMapping 수신
//    @SendTo("/topic/{roomId}") // 해당 roomId에 메시지를 발행하여 구독중인 클라이언트에게 메시지 전송
//    // DestinationVariable : @MessageMapping 어노테이션으로 정의된 Websocket Controller 내에서만 사용
//    public String sendMessage(@DestinationVariable Long roomId, String message) {
//        System.out.println(message);
//
//        return message;
//    }

    // 방법 2. MessageMapping 어노테이션만 활용
    // 어노테이션 사용 시 소스 코드의 유연성 떨어지기 때문에 방법 1대신 방법 2 사용
    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageDto chatMessageDto) {
        // System.out.println("보낸 사람: " + chatMessageDto.getSenderEmail() + " 메시지: " + chatMessageDto.getMessage());
        chatService.saveMessage(roomId, chatMessageDto);
        messageTemplate.convertAndSend("/topic/"+roomId, chatMessageDto);
    }

}