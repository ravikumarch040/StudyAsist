"""Shared assessments - challenge codes."""
import secrets
from datetime import datetime, timedelta

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from pydantic import BaseModel

from app.database import get_db
from app.models import User, SharedAssessment
from app.auth import require_user, get_current_user

router = APIRouter(prefix="/share", tags=["share"])


class CreateShareRequest(BaseModel):
    """Create shareable challenge."""

    title: str
    assessment_data: dict
    expires_hours: int = 24


class ShareResponse(BaseModel):
    """Share code response."""

    code: str
    expires_at: str


def _generate_code() -> str:
    """Generate short unique code."""
    return secrets.token_hex(4).upper()[:8]


@router.post("/create", response_model=ShareResponse)
def create_share(
    data: CreateShareRequest,
    user: User = Depends(require_user),
    db: Session = Depends(get_db),
):
    """Create shareable assessment challenge code."""
    code = _generate_code()
    expires = datetime.utcnow() + timedelta(hours=data.expires_hours)
    row = SharedAssessment(
        code=code,
        owner_id=user.id,
        title=data.title,
        assessment_data=data.assessment_data,
        expires_at=expires,
    )
    db.add(row)
    db.commit()
    return ShareResponse(code=code, expires_at=expires.isoformat())


@router.get("/resolve/{code}")
def resolve_share(
    code: str,
    db: Session = Depends(get_db),
):
    """Resolve challenge code to assessment data. No auth required."""
    row = db.query(SharedAssessment).filter(SharedAssessment.code == code.upper()).first()
    if not row:
        raise HTTPException(status_code=404, detail="Challenge not found")
    if row.expires_at and row.expires_at < datetime.utcnow():
        raise HTTPException(status_code=410, detail="Challenge expired")
    return {
        "title": row.title,
        "assessment_data": row.assessment_data,
    }
