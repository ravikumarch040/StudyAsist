"""Google ID token verification."""
from typing import Optional

from google.oauth2 import id_token
from google.auth.transport import requests

from app.config import settings


def verify_google_token(id_token_str: str) -> Optional[dict]:
    """
    Verify Google ID token and return payload with email, name, sub.
    Returns None if invalid.
    """
    if not settings.google_client_id:
        return None
    try:
        payload = id_token.verify_oauth2_token(
            id_token_str,
            requests.Request(),
            settings.google_client_id,
        )
        return payload
    except Exception:
        return None
