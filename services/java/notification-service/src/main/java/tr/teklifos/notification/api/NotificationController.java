package tr.teklifos.notification.api;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    @GetMapping
    public List<NotificationSummary> list() {
        return List.of();
    }

    public record NotificationSummary(String title, String channel, boolean read) {}
}
