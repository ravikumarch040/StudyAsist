"""Leaderboard API - submit scores, fetch rankings."""
from typing import Optional
from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from pydantic import BaseModel

from app.database import get_db
from app.models import User, LeaderboardEntry
from app.auth import require_user

router = APIRouter(prefix="/leaderboard", tags=["leaderboard"])


class SubmitScoreRequest(BaseModel):
    """Submit assessment score to leaderboard."""

    score: float
    max_score: float
    assessment_title: Optional[str] = None
    goal_name: Optional[str] = None
    streak_days: int = 0


class LeaderboardItem(BaseModel):
    """Leaderboard entry for response."""

    rank: int
    user_name: str
    score: float
    max_score: float
    percentage: float
    streak_days: int


@router.post("/submit")
def submit_score(
    data: SubmitScoreRequest,
    user: User = Depends(require_user),
    db: Session = Depends(get_db),
):
    """Submit a score to the leaderboard."""
    entry = LeaderboardEntry(
        user_id=user.id,
        score=data.score,
        max_score=data.max_score,
        assessment_title=data.assessment_title,
        goal_name=data.goal_name,
        streak_days=data.streak_days,
    )
    db.add(entry)
    db.commit()
    return {"ok": True, "id": entry.id}


@router.get("/top", response_model=list)
def get_top(
    limit: int = Query(default=50, le=100),
    db: Session = Depends(get_db),
):
    """Get top scores (all users). No auth required for viewing."""
    entries = (
        db.query(LeaderboardEntry, User)
        .join(User, LeaderboardEntry.user_id == User.id)
        .order_by((LeaderboardEntry.score / LeaderboardEntry.max_score).desc())
        .limit(limit)
        .all()
    )
    result = []
    for rank, (entry, u) in enumerate(entries, 1):
        pct = (entry.score / entry.max_score * 100) if entry.max_score else 0
        result.append({
            "rank": rank,
            "user_name": u.name or u.email.split("@")[0],
            "score": entry.score,
            "max_score": entry.max_score,
            "percentage": round(pct, 1),
            "streak_days": entry.streak_days,
        })
    return result


@router.get("/me")
def get_my_scores(
    user: User = Depends(require_user),
    db: Session = Depends(get_db),
    limit: int = Query(default=20, le=50),
):
    """Get current user's recent scores."""
    entries = (
        db.query(LeaderboardEntry)
        .filter(LeaderboardEntry.user_id == user.id)
        .order_by(LeaderboardEntry.created_at.desc())
        .limit(limit)
        .all()
    )
    return [
        {
            "score": e.score,
            "max_score": e.max_score,
            "assessment_title": e.assessment_title,
            "goal_name": e.goal_name,
            "streak_days": e.streak_days,
            "created_at": e.created_at.isoformat() if e.created_at else None,
        }
        for e in entries
    ]
