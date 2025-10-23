package com.goojakgyo.goojakgyo.chat.repository;

import com.goojakgyo.goojakgyo.chat.domain.ChatRoom;
import com.goojakgyo.goojakgyo.chat.domain.ReadStatus;
import com.goojakgyo.goojakgyo.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadStatusRepository extends JpaRepository<ReadStatus, Long> {
    List<ReadStatus> findByChatRoomAndMember(ChatRoom chatRoom, Member member);
    Long countByChatRoomAndMemberAndIsReadFalse(ChatRoom chatRoom, Member member); // 현재 채팅방에서 안 읽은 메시지 개수 리턴
}
