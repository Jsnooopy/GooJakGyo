package com.goojakgyo.goojakgyo.chat.domain;

import com.goojakgyo.goojakgyo.common.domain.BaseTimeEntity;
import com.goojakgyo.goojakgyo.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 채팅 참여자 Entity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ChatParticipant extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    // 관계성을 가졌을 때 만약에 참조하면 그 때 조회하겠음
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 1대1 채팅일 때 상대방의 이름이 채팅방 이름이 되도록 (사용자 이름이 변경되지 않는다고 가정)
    private String displayName;
}
