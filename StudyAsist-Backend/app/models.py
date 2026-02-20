"""SQLAlchemy models for StudyAsist backend."""
from datetime import datetime
from sqlalchemy import String, Integer, Float, DateTime, ForeignKey, JSON, Column
from sqlalchemy.orm import relationship

from app.database import Base


class User(Base):
    """User account for sync and leaderboards."""

    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    email = Column(String(255), unique=True, index=True, nullable=False)
    name = Column(String(255))
    google_id = Column(String(255), unique=True, index=True)
    apple_id = Column(String(255), unique=True, index=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    leaderboard_entries = relationship("LeaderboardEntry", back_populates="user")
    shared_assessments = relationship("SharedAssessment", back_populates="owner")


class LeaderboardEntry(Base):
    """Leaderboard score entry for gamification."""

    __tablename__ = "leaderboard_entries"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    score = Column(Float, nullable=False)
    max_score = Column(Float, nullable=False)
    assessment_title = Column(String(255))
    goal_name = Column(String(255))
    streak_days = Column(Integer, default=0)
    created_at = Column(DateTime, default=datetime.utcnow)

    user = relationship("User", back_populates="leaderboard_entries")


class SharedAssessment(Base):
    """Shareable assessment challenge codes."""

    __tablename__ = "shared_assessments"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    code = Column(String(16), unique=True, index=True, nullable=False)
    owner_id = Column(Integer, ForeignKey("users.id"))
    assessment_data = Column(JSON)
    title = Column(String(255))
    expires_at = Column(DateTime)
    created_at = Column(DateTime, default=datetime.utcnow)

    owner = relationship("User", back_populates="shared_assessments")


class SyncPayload(Base):
    """User data sync payload (timetables, goals, Q&A, etc.)."""

    __tablename__ = "sync_payloads"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    payload = Column(JSON, nullable=False)
    version = Column(Integer, default=1)
    created_at = Column(DateTime, default=datetime.utcnow)
