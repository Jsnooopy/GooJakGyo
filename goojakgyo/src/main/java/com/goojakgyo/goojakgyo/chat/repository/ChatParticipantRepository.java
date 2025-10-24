package com.goojakgyo.goojakgyo.chat.repository;

import com.goojakgyo.goojakgyo.chat.domain.ChatParticipant;
import com.goojakgyo.goojakgyo.chat.domain.ChatRoom;
import com.goojakgyo.goojakgyo.member.domain.Member;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);
    Optional<ChatParticipant> findByChatRoomAndMember(ChatRoom chatRoom, Member member);
    List<ChatParticipant> findAllByMember(Member member);

    // JPQL : 내 ID, 상대방 ID 넘겨서 개인 채팅방이 이미 존재하는지 확인
    @Query("""
            SELECT cp1.chatRoom
            FROM ChatParticipant cp1
            JOIN ChatParticipant cp2
            ON cp1.chatRoom.id = cp2.chatRoom.id
            WHERE cp1.member.id = :myId
            AND cp2.member.id = :otherMemberId
            AND cp1.chatRoom.isGroupChat = 'N'
            """)
    Optional<ChatRoom> findExistingPrivateRoom(@Param("myId")Long myId, @Param("otherMemberId")Long otherMemberId);
}
