"""Auth endpoints - login, register, token refresh, Google/Apple sign-in."""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from pydantic import BaseModel, EmailStr

from app.database import get_db
from app.models import User
from app.auth import create_access_token, require_user
from app.auth_google import verify_google_token
from app.auth_apple import verify_apple_token

router = APIRouter(prefix="/auth", tags=["auth"])


class TokenResponse(BaseModel):
    """JWT token response."""

    access_token: str
    token_type: str = "bearer"


class IdTokenRequest(BaseModel):
    """ID token from Google or Apple OAuth."""

    id_token: str


class LoginRequest(BaseModel):
    """Email/password or Google ID token login."""

    email: EmailStr
    name: str | None = None
    google_id: str | None = None
    apple_id: str | None = None


def _get_or_create_user(db: Session, email: str, name: str, google_id: str | None, apple_id: str | None) -> User:
    """Find or create user by email; link Google/Apple IDs if provided."""
    user = db.query(User).filter(User.email == email).first()
    if not user:
        user = User(
            email=email,
            name=name,
            google_id=google_id,
            apple_id=apple_id,
        )
        db.add(user)
        db.commit()
        db.refresh(user)
    else:
        if google_id and not user.google_id:
            user.google_id = google_id
        if apple_id and not user.apple_id:
            user.apple_id = apple_id
        if name:
            user.name = name
        db.commit()
    return user


@router.post("/google", response_model=TokenResponse)
def login_google(data: IdTokenRequest, db: Session = Depends(get_db)):
    """Verify Google ID token, create/find user, return JWT."""
    payload = verify_google_token(data.id_token)
    if not payload:
        raise HTTPException(status_code=401, detail="Invalid Google token")
    email = payload.get("email")
    if not email:
        raise HTTPException(status_code=401, detail="Google token missing email")
    name = payload.get("name") or email.split("@")[0]
    google_id = payload.get("sub")
    user = _get_or_create_user(db, email, name, google_id=google_id, apple_id=None)
    token = create_access_token({"sub": str(user.id)})
    return TokenResponse(access_token=token)


@router.post("/apple", response_model=TokenResponse)
def login_apple(data: IdTokenRequest, db: Session = Depends(get_db)):
    """Verify Apple identity token, create/find user, return JWT."""
    payload = verify_apple_token(data.id_token)
    if not payload:
        raise HTTPException(status_code=401, detail="Invalid Apple token")
    apple_id = payload.get("sub")
    if not apple_id:
        raise HTTPException(status_code=401, detail="Apple token missing sub")
    email = payload.get("email")
    # Apple may omit email on subsequent logins; use sub-based placeholder if needed
    if not email:
        user = db.query(User).filter(User.apple_id == apple_id).first()
        if user:
            email = user.email
        else:
            raise HTTPException(status_code=400, detail="Apple token missing email (first-time sign-in only)")
    name = payload.get("name") or email.split("@")[0]
    user = _get_or_create_user(db, email, name, google_id=None, apple_id=apple_id)
    token = create_access_token({"sub": str(user.id)})
    return TokenResponse(access_token=token)


@router.post("/login", response_model=TokenResponse)
def login(data: LoginRequest, db: Session = Depends(get_db)):
    """Legacy login: register by email. Prefer /google or /apple for token-based auth."""
    user = _get_or_create_user(
        db, data.email, data.name or data.email.split("@")[0], data.google_id, data.apple_id
    )
    token = create_access_token({"sub": str(user.id)})
    return TokenResponse(access_token=token)


@router.get("/me")
def me(user: User = Depends(require_user)):
    """Get current user. Requires valid token."""
    return {"id": user.id, "email": user.email, "name": user.name}
