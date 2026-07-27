package tr.teklifos.masterdata.application;

import org.springframework.stereotype.Component;

@Component
public class SkuNormalizer {

    private final TextNormalizer textNormalizer;

    public SkuNormalizer(TextNormalizer textNormalizer) {
        this.textNormalizer = textNormalizer;
    }

    public String normalize(String sku) {
        return textNormalizer.normalize(sku).replaceAll("[^a-z0-9]", "");
    }
}
