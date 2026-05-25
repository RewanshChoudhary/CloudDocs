
### Phase 1 — Python fundamentals you'll hit on day one

These will block you immediately if you skip them.

**Python basics** — variables, functions, loops, conditionals, list comprehensions. Don't linger here. Two days max. You already know programming logic from other languages if any.

**`pyproject.toml` and virtual environments** — before writing a single line, understand what `uv` or `poetry` does, why dependencies are pinned, and what a virtual environment isolates. This is day one of any real project.

**Type hints** — `def run(self, context: ResearchContext) -> AgentResult`. You'll write this constantly. Understand `Optional`, `list[str]`, `dict[str, Any]`. Python doesn't enforce them at runtime but your editor and colleagues depend on them.

**Dataclasses** — `context.py` is a dataclass. Learn `@dataclass`, `field()`, and why it beats writing `__init__` by hand.

**`__init__.py` and imports** — why packages need them, relative vs absolute imports, and what `from worker.agents.base_agent import BaseAgent` actually resolves to on disk.

---

### Phase 2 — Object-oriented Python for `base_agent.py`

This is where your architecture begins.

**Classes and `__init__`** — instantiation, `self`, instance vs class variables.

**Inheritance** — `SearchAgent(BaseAgent)`. What gets inherited, what gets overridden, and how `super().__init__()` works.

**Abstract classes** — `from abc import ABC, abstractmethod`. This is exactly what `base_agent.py` uses. Understand why `abstractmethod` makes Python raise an error if a subclass forgets to implement `run()`. This is the enforcement mechanism for your pipeline contract.

**`@property` and `@staticmethod`** — you'll use these in agent classes. Know the difference.

**Dunder methods** — at minimum `__repr__` and `__str__`. Useful when debugging context objects mid-pipeline.

---

### Phase 3 — Pydantic for `db/schemas.py` and `core/settings.py`

Pydantic is the backbone of your data layer. Learn it before touching the database.

**`BaseModel`** — define a model, validate input, parse from dict. Understand what happens when validation fails.

**Field types and validators** — `@field_validator`, `model_validator`. Your `ResearchRequest` needs these for the domain enum and depth validation from Phase 1 of your workflow.

**`BaseSettings`** — how it reads from `.env`, why secrets never hardcode in source. Write `settings.py` as your first real file in the project.

**Nested models** — `FactCheckResult` contains a list of `Claim` objects. Understand how Pydantic serializes and deserializes nested structures.

**`.model_dump()` and `.model_validate()`** — converting between Pydantic models and plain dicts. You'll use this when writing to and reading from the database.

---

### Phase 4 — SQLAlchemy for `db/models.py`

The database layer. Learn the ORM, not raw SQL first.

**SQLAlchemy Core vs ORM** — understand the distinction. You'll use ORM. Know why.

**Declarative models** — `Base`, `Column`, mapped Python classes to tables. Write `ResearchJob`, `ResearchReport`, `Source` as your first models.

**Relationships** — a `ResearchJob` has many `Source` records. Understand `relationship()`, `ForeignKey`, and lazy vs eager loading. This will matter when you query a job and need its sources.

**Sessions and transactions** — `with Session() as session`. Why sessions exist, what a transaction boundary is, and what happens if you forget to commit.

**Alembic for migrations** — when you change a model, the database doesn't update automatically. Alembic generates migration scripts. Learn `alembic revision --autogenerate` and `alembic upgrade head` before you need them.

---

### Phase 5 — Async Python for `scraper_agent.py`

The scraper runs all URLs concurrently. This requires understanding async properly.

**`async`/`await` basics** — what makes a function a coroutine, why you need `await` to call it, and what happens if you forget.

**`asyncio.gather()`** — this is the exact function your scraper uses to fire all HTTP requests simultaneously. Understand what it returns and how to handle partial failures when some URLs succeed and others fail.

**`httpx` async client** — the library for async HTTP requests in Python. Write a small script that fetches 5 URLs concurrently before touching the actual scraper.

**Error handling in async** — `try/except` inside coroutines, and why exceptions inside `gather()` need special handling with `return_exceptions=True`.

**When not to use async** — understand that CPU-bound work doesn't benefit from async. Your LLM calls are IO-bound (waiting for network), so async helps there. Your confidence score calculation is CPU-bound — no async needed.

---

### Phase 6 — HTTP clients and external APIs for `core/llm_client.py`

**`httpx`** — your HTTP library for both sync and async. Understand `Client` vs `AsyncClient`, timeouts, and response status codes.

**Retry logic with `tenacity`** — the library your LLM client uses for exponential backoff. Understand `@retry`, `wait_exponential`, `stop_after_attempt`. This is what handles the "LLM API rate limit" row in your failure handling table.

**Environment-based API keys** — never hardcode. Understand how `settings.py` exposes `settings.llm_api_key` and how `llm_client.py` consumes it.

**Parsing JSON responses** — LLM APIs return JSON. Understand `response.json()`, and how you feed the response into a Pydantic model for validation.

---

### Phase 7 — Redis for `core/` cache calls

**What Redis is** — a key-value store in memory. Faster than a database for ephemeral data like caches and counters.

**`redis-py`** — the Python client. Learn `get`, `set`, `setex` (set with TTL), `incr`. These are the only four operations your project uses.

**TTL (time-to-live)** — why summary cache entries expire after 24 hours and rate limit counters expire after 1 hour. Understand `setex` vs `set`.

**The duplicate detection pattern** — your workflow hashes `userid + query + domain + depth` as a Redis key. Understand why Redis is used here instead of querying the database every time.

---

### Phase 8 — Testing with `pytest`

**`pytest` basics** — writing test functions, `assert`, running with `pytest tests/`.

**Fixtures with `conftest.py`** — shared setup like a test database or a mock HTTP client. Understand `@pytest.fixture` and `scope`.

**Mocking with `unittest.mock`** — `patch`, `MagicMock`. You will mock the LLM API in every agent test. Without this skill your tests will make real API calls and cost money.

**`pytest-asyncio`** — testing async functions. Your scraper tests need this.

**What to test first** — `orchestrator.py` with mocked agents. Prove the pipeline builds correctly for QUICK, STANDARD, and DEEP depth before testing any individual agent. Then test agents in isolation with mocked LLM responses.

---

### Phase 9 — Tooling that makes the project professional

These aren't features but they're what separate a student project from a portfolio project.

**`ruff`** — linter and formatter. One tool replacing `flake8` + `black`. Add it to `pyproject.toml`, run it in CI.

**`mypy`** — static type checker. Catches type errors before runtime. Run it alongside `ruff`.

**`pre-commit`** — runs `ruff` and `mypy` automatically before every git commit. This is what prevents bad code from ever entering the repo.

**GitHub Actions** — a `.github/workflows/ci.yml` that runs `ruff`, `mypy`, and `pytest` on every push. This is the single biggest signal to a reviewer that you understand production workflows. It takes 20 lines of YAML.

**Structured logging with `structlog`** — instead of `print()`, use structured JSON logs. Every log entry in your pipeline should include `job_id`, `agent_name`, `duration_ms`. This is what real systems use.

---

### The order you actually build it in

```
settings.py          → proves your environment works
db/models.py         → tables exist before any agent needs them  
db/schemas.py        → Pydantic contracts before any agent I/O
core/context.py      → the object traveling through the whole pipeline
base_agent.py        → the contract before any agent is written
core/llm_client.py   → LLM wrapper before any agent calls it
search_agent.py      → first agent, no LLM needed for basic flow
orchestrator.py      → wire search into a minimal pipeline and run it
scraper_agent.py     → add async, test gather()
summarizer_agent.py  → first real LLM calls in the pipeline
... remaining agents  → each one builds on what you've already learned
tests/               → write tests alongside each file, not after
CI pipeline          → set up GitHub Actions before the project is done
```