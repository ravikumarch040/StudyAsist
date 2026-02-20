"""StudyAsist Backend API - FastAPI entry point."""
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings
from app.database import engine, Base, get_db
from app import models  # noqa: F401 - register models before create_all
from app.routers import auth_router, leaderboard_router, sync_router, share_router


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Create tables on startup."""
    Base.metadata.create_all(bind=engine)
    yield


app = FastAPI(
    title="StudyAsist API",
    description="Backend for StudyAsist - auth, sync, leaderboards, shared assessments",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins.split(",") if settings.cors_origins else ["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth_router.router, prefix="/api")
app.include_router(leaderboard_router.router, prefix="/api")
app.include_router(sync_router.router, prefix="/api")
app.include_router(share_router.router, prefix="/api")


@app.get("/")
def root():
    """Health check."""
    return {"status": "ok", "service": "StudyAsist API"}


@app.get("/health")
def health():
    """Health check for load balancers."""
    return {"status": "healthy"}
