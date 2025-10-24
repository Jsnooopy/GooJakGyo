package com.goojakgyo.goojakgyo.chat.repository;

import com.goojakgyo.goojakgyo.chat.domain.ChatMessage;
import com.goojakgyo.goojakgyo.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findByIsGroupChat(String isGroupChat);
}
