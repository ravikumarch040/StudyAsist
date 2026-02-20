# StudyAsist Backend

Python FastAPI backend for StudyAsist – auth, sync, leaderboards, shared assessments.

## Setup

```bash
cd StudyAsist-Backend
python -m venv venv
venv\Scripts\activate   # Windows
pip install -r requirements.txt
```

## Run

```bash
uvicorn main:app --reload
```

API: http://localhost:8000  
Docs: http://localhost:8000/docs

## Environment (.env)

```
DATABASE_URL=sqlite:///./studyasist.db
SECRET_KEY=your-secret-key-here
CORS_ORIGINS=*
```

## API Endpoints

| Endpoint | Description |
|----------|-------------|
| `POST /api/auth/login` | Login/register (email, google_id, apple_id) |
| `GET /api/auth/me` | Current user (Bearer token) |
| `POST /api/leaderboard/submit` | Submit score |
| `GET /api/leaderboard/top` | Top scores |
| `GET /api/leaderboard/me` | My scores |
| `POST /api/sync/upload` | Upload sync payload |
| `GET /api/sync/download` | Download sync payload |
| `POST /api/share/create` | Create challenge code |
| `GET /api/share/resolve/{code}` | Resolve challenge code |
