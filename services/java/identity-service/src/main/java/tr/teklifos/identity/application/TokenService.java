package tr.teklifos.identity.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.teklifos.identity.config.JwtKeyProperties;
import tr.teklifos.identity.domain.AppUserEntity;
import tr.teklifos.identity.domain.RefreshTokenEntity;
import tr.teklifos.identity.infrastructure.AppUserRepository;
import tr.teklifos.identity.infrastructure.PermissionRepository;
import tr.teklifos.identity.infrastructure.RefreshTokenRepository;
import tr.teklifos.shared.security.JwtTenantResolver;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtKeyProperties props;
    private final PermissionRepository permissionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AppUserRepository appUserRepository;

    public TokenService(
            JwtEncoder jwtEncoder,
            JwtKeyProperties props,
            PermissionRepository permissionRepository,
            RefreshTokenRepository refreshTokenRepository,
            AppUserRepository appUserRepository) {
        this.jwtEncoder = jwtEncoder;
        this.props = props;
        this.permissionRepository = permissionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public TokenPair issueTokens(AppUserEntity user) {
        List<String> permissions =
                permissionRepository.findPermissionsForUser(user.getId()).stream()
                        .map(p -> p.getCode())
                        .distinct()
                        .toList();

        Instant now = Instant.now();
        Duration accessTtl = Duration.parse(props.accessTokenTtl());
        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer(props.issuer())
                        .issuedAt(now)
                        .expiresAt(now.plus(accessTtl))
                        .subject(user.getId().toString())
                        .claim(JwtTenantResolver.CLAIM_TENANT, user.getTenantId().toString())
                        .claim(JwtTenantResolver.CLAIM_PERMISSIONS, permissions)
                        .build();

        String accessToken =
                jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        String rawRefresh = UUID.randomUUID() + "." + UUID.randomUUID();
        RefreshTokenEntity rt = new RefreshTokenEntity();
        rt.setTenantId(user.getTenantId());
        rt.setUserId(user.getId());
        rt.setTokenHash(hash(rawRefresh));
        rt.setFamilyId(UUID.randomUUID());
        rt.setExpiresAt(now.plus(Duration.parse(props.refreshTokenTtl())));
        refreshTokenRepository.save(rt);

        return new TokenPair(accessToken, rawRefresh, accessTtl.getSeconds());
    }

    @Transactional
    public TokenPair rotateRefresh(String rawRefresh) {
        String hash = hash(rawRefresh);
        RefreshTokenEntity existing =
                refreshTokenRepository
                        .findByTokenHashAndRevokedAtIsNull(hash)
                        .filter(rt -> rt.getExpiresAt().isAfter(Instant.now()))
                        .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        existing.setRevokedAt(Instant.now());
        refreshTokenRepository.save(existing);

        AppUserEntity user =
                appUserRepository
                        .findById(existing.getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        return issueTokens(user);
    }

    public static String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of()
                    .formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public record TokenPair(String accessToken, String refreshToken, long expiresInSeconds) {}
}
