package com.goojakgyo.goojakgyo.chat.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 내가 속한 채팅방 목록
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyChatListResDto {
    private Long roomId;
    private String roomName;
    private String isGroupChat;
    private Long unReadCount;
}
