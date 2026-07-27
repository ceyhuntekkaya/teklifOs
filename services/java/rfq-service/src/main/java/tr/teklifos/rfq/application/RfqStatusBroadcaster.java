package tr.teklifos.rfq.application;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class RfqStatusBroadcaster {

    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(UUID tenantId, UUID rfqId) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.put(rfqId, emitter);
        emitter.onCompletion(() -> emitters.remove(rfqId));
        emitter.onTimeout(() -> emitters.remove(rfqId));
        return emitter;
    }

    public void broadcast(UUID tenantId, UUID rfqId, String status) {
        SseEmitter emitter = emitters.get(rfqId);
        if (emitter == null) {
            return;
        }
        try {
            emitter.send(SseEmitter.event().name("status").data(status));
        } catch (Exception ignored) {
            emitters.remove(rfqId);
        }
    }
}
