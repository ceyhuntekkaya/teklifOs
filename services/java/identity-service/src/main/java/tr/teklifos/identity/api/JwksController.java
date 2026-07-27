package tr.teklifos.identity.api;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.teklifos.identity.config.JwtKeyProperties;

@RestController
public class JwksController {

    private final JwtKeyProperties keyProperties;

    public JwksController(JwtKeyProperties keyProperties) {
        this.keyProperties = keyProperties;
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        RSAKey rsaKey = new RSAKey.Builder(keyProperties.publicKey()).build();
        return new JWKSet(rsaKey).toJSONObject();
    }
}
