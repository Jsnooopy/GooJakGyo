package com.goojakgyo.goojakgyo.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseService {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // SSE 연결 유지할 최대 시간 : 1시간

    // SSE 구독 시도
    public SseEmitter subscribe(Long memberId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitters.put(memberId, emitter); // memberId에게 이제부터 Event를 보내겠다

        emitter.onCompletion(() -> emitters.remove(memberId));
        emitter.onTimeout(() -> emitters.remove(memberId));
        emitter.onError((e) -> emitters.remove(memberId));

        // 연결 성공했을 때
        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch(Exception ignored) {
            log.debug("SSE connection already closed.");
        }

        return emitter;
    }

    // ChatService에서 호출됨
    // 채팅방에서 메시지 보냈을 때 해당 채팅방에 속한 유저에게 SSE 전송
    public void sendNewMessageEvent(Long memberId, Long roomId, Long unReadCount) {
        SseEmitter emitter = emitters.get(memberId);
        if(emitter != null) {
            System.out.println("roomId: " + roomId + " unReadCount: " + unReadCount);
            try {
                emitter.send(
                        SseEmitter.event()
                                .name("new-message")
                                .data(Map.of("roomId", roomId, "unReadCount", unReadCount))
                );
            } catch (Exception e) {
                emitters.remove(memberId);
            }
        }
    }

}
