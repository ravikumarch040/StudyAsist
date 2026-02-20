"""Application configuration."""
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """Backend settings loaded from env."""

    # Database
    database_url: str = "sqlite:///./studyasist.db"
    # Use PostgreSQL in prod: postgresql://user:pass@localhost/studyasist

    # Auth
    secret_key: str = "change-me-in-production-use-openssl-rand-hex-32"
    algorithm: str = "HS256"
    access_token_expire_minutes: int = 60 * 24 * 7  # 7 days
    # Google OAuth - Web client ID for ID token verification (from Google Cloud Console)
    google_client_id: str | None = None
    # Apple Sign In - Service ID / App ID for verification (optional; Apple tokens are self-signed JWTs)
    apple_bundle_id: str | None = None

    # CORS
    cors_origins: str = "*"

    # Redis (optional, for caching / rate limiting)
    redis_url: str | None = None

    class Config:
        env_file = ".env"
        extra = "ignore"


settings = Settings()
