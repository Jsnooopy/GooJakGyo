package com.goojakgyo.goojakgyo.chat.repository;

import com.goojakgyo.goojakgyo.chat.domain.ChatMessage;
import com.goojakgyo.goojakgyo.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomOrderByCreatedTimeAsc(ChatRoom chatRoom); // 생성 시간 오름차순으로 정렬
    Optional<ChatMessage> findTopByChatRoomOrderByIdDesc(ChatRoom chatRoom); // 현재 채팅방에서 마지막으로 보낸 메시지 ID 리턴
    Long countByChatRoomAndIdGreaterThan(ChatRoom chatRoom, Long messageId); // 특정 ID 이후 메시지 개수 (안 읽은 메시지)
    Long countByChatRoom(ChatRoom chatRoom); // 현재 채팅방의 전체 메시지 개수
}
