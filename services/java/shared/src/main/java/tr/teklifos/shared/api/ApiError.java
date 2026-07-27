package tr.teklifos.shared.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.net.URI;
import java.time.Instant;
import java.util.Map;

/** RFC 9457 Problem Details for HTTP APIs. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        URI type,
        String title,
        int status,
        String detail,
        URI instance,
        String code,
        Instant timestamp,
        Map<String, Object> extensions) {

    public static ApiError of(int status, String title, String detail, String code) {
        return new ApiError(
                URI.create("about:blank"),
                title,
                status,
                detail,
                null,
                code,
                Instant.now(),
                null);
    }
}
