package tr.teklifos.masterdata.application;

import java.text.Normalizer;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class TextNormalizer {

    public String normalize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String trimmed = text.trim().toLowerCase(Locale.ROOT);
        return Normalizer.normalize(trimmed, Normalizer.Form.NFKD).replaceAll("\\p{M}", "");
    }
}
