package tr.teklifos.identity.api;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final tr.teklifos.identity.application.AuthenticationService authenticationService;
    private final tr.teklifos.identity.application.TokenService tokenService;

    public AuthController(
            tr.teklifos.identity.application.AuthenticationService authenticationService,
            tr.teklifos.identity.application.TokenService tokenService) {
        this.authenticationService = authenticationService;
        this.tokenService = tokenService;
    }

    @org.springframework.web.bind.annotation.PostMapping("/api/v1/auth/login")
    public TokenResponse login(@org.springframework.web.bind.annotation.RequestBody LoginRequest body) {
        var pair = authenticationService.login(body.email(), body.password(), body.tenantSlug());
        return TokenResponse.from(pair);
    }

    @org.springframework.web.bind.annotation.PostMapping("/api/v1/auth/refresh")
    public TokenResponse refresh(
            @org.springframework.web.bind.annotation.RequestBody RefreshRequest body) {
        return TokenResponse.from(tokenService.rotateRefresh(body.refreshToken()));
    }

    public record LoginRequest(String email, String password, String tenantSlug) {}

    public record RefreshRequest(String refreshToken) {}

    public record TokenResponse(String accessToken, String refreshToken, long expiresIn) {
        static TokenResponse from(tr.teklifos.identity.application.TokenService.TokenPair pair) {
            return new TokenResponse(pair.accessToken(), pair.refreshToken(), pair.expiresInSeconds());
        }
    }
}
