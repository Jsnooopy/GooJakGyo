package com.goojakgyo.goojakgyo.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 채팅 메시지 Dto
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private String message;
    private String senderEmail;
    private String senderName; // 보낸 사람 이름 추가
}
