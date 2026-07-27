package tr.teklifos.masterdata.application;

import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class UnitNormalizer {

    public String normalize(String unitCode) {
        if (unitCode == null || unitCode.isBlank()) {
            return "EA";
        }
        return unitCode.trim().toUpperCase(Locale.ROOT);
    }
}
