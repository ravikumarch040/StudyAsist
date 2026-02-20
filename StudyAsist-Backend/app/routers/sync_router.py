"""Data sync API - upload/download user data for cross-device sync."""
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from pydantic import BaseModel

from app.database import get_db
from app.models import User, SyncPayload
from app.auth import require_user

router = APIRouter(prefix="/sync", tags=["sync"])


class SyncUploadRequest(BaseModel):
    """Upload sync payload (timetables, goals, Q&A, etc.)."""

    payload: dict
    version: int = 1


class SyncDownloadResponse(BaseModel):
    """Download sync payload."""

    payload: dict
    version: int


@router.post("/upload")
def upload_sync(
    data: SyncUploadRequest,
    user: User = Depends(require_user),
    db: Session = Depends(get_db),
):
    """Upload user data for sync."""
    row = SyncPayload(user_id=user.id, payload=data.payload, version=data.version)
    db.add(row)
    db.commit()
    return {"ok": True, "id": row.id}


@router.get("/download", response_model=SyncDownloadResponse)
def download_sync(
    user: User = Depends(require_user),
    db: Session = Depends(get_db),
):
    """Download latest sync payload for user."""
    row = (
        db.query(SyncPayload)
        .filter(SyncPayload.user_id == user.id)
        .order_by(SyncPayload.created_at.desc())
        .first()
    )
    if not row:
        return SyncDownloadResponse(payload={}, version=0)
    return SyncDownloadResponse(payload=row.payload, version=row.version)
