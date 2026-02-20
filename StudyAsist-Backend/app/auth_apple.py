"""Apple ID token verification."""
import jwt
from jwt import PyJWKClient
from typing import Optional

from app.config import settings

APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys"
APPLE_ISSUER = "https://appleid.apple.com"


def verify_apple_token(identity_token: str) -> Optional[dict]:
    """
    Verify Apple identity token (JWT) and return payload with email, sub.
    Apple may not send email on subsequent logins - sub is the stable user id.
    Returns None if invalid.
    """
    audience = settings.apple_bundle_id or "com.studyasist"
    try:
        jwks_client = PyJWKClient(APPLE_JWKS_URL)
        signing_key = jwks_client.get_signing_key_from_jwt(identity_token)
        payload = jwt.decode(
            identity_token,
            signing_key.key,
            algorithms=["RS256"],
            issuer=APPLE_ISSUER,
            audience=audience,
        )
        return payload
    except Exception:
        return None
