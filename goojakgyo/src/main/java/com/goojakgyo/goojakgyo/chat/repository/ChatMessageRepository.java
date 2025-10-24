package com.goojakgyo.goojakgyo.chat.repository;

import com.goojakgyo.goojakgyo.chat.domain.ChatMessage;
import com.goojakgyo.goojakgyo.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomOrderByCreatedTimeAsc(ChatRoom chatRoom); // 생성 시간 오름차순으로 정렬
}
