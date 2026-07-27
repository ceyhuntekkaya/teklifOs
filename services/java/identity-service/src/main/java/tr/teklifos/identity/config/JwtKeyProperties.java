package tr.teklifos.identity.config;

import java.security.Key;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "teklifos.jwt")
public record JwtKeyProperties(
        String issuer,
        String accessTokenTtl,
        String refreshTokenTtl,
        Resource privateKeyPath,
        Resource publicKeyPath) {

    public RSAPrivateKey privateKey() {
        return (RSAPrivateKey) readKey(privateKeyPath, true);
    }

    public RSAPublicKey publicKey() {
        return (RSAPublicKey) readKey(publicKeyPath, false);
    }

    private static java.security.Key readKey(Resource resource, boolean privateKey) {
        try {
            String pem = Files.readString(Path.of(resource.getURI()));
            String normalized =
                    pem.replace("-----BEGIN PRIVATE KEY-----", "")
                            .replace("-----END PRIVATE KEY-----", "")
                            .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                            .replace("-----END RSA PRIVATE KEY-----", "")
                            .replace("-----BEGIN PUBLIC KEY-----", "")
                            .replace("-----END PUBLIC KEY-----", "")
                            .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(normalized);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            if (privateKey) {
                return kf.generatePrivate(new PKCS8EncodedKeySpec(decoded));
            }
            return kf.generatePublic(new X509EncodedKeySpec(decoded));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load JWT key", e);
        }
    }
}
