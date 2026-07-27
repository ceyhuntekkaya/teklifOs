from dataclasses import dataclass
from typing import Any

import jwt


@dataclass
class JwtValidator:
    """Stub JWT validator; replace with JWKS-backed verification in production."""

    issuer: str
    audience: str | None = None
    jwks_url: str | None = None

    def validate(self, token: str) -> dict[str, Any]:
        """Decode and validate a bearer token (stub: no signature verification)."""
        options = {"verify_signature": False}
        return jwt.decode(
            token,
            options=options,
            algorithms=["RS256", "HS256"],
            issuer=self.issuer,
            audience=self.audience,
        )

    def validate_authorization_header(self, authorization: str | None) -> dict[str, Any] | None:
        if not authorization or not authorization.startswith("Bearer "):
            return None
        return self.validate(authorization.removeprefix("Bearer ").strip())
