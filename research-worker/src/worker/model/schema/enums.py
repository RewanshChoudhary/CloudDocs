from enum import Enum

class Domain(str, Enum):
    GENERAL = "general"
    MEDICAL = "medical"
    LEGAL = "legal"

class Depth(str, Enum):
    QUICK = "quick"
    STANDARD = "standard"
    DEEP = "deep"

class OutputFormat(str, Enum):
    JSON = "json"
    MARKDOWN = "markdown"
    PLAIN = "plain"

class JobStatus(str, Enum):
    PENDING = "pending"
    PROCESSING = "processing"
    COMPLETED = "completed"
    FAILED = "failed"

class PipelineStage(str, Enum):
    SEARCHING = "searching"
    SCRAPING = "scraping"
    SUMMARIZING = "summarizing"
    FACT_CHECKING = "fact_checking"
    ANALYZING = "analyzing"
    BUILDING = "building"

class SourceStatus(str, Enum):
    PENDING = "pending"
    SUCCESS = "success"
    FAILED = "failed"
    BLOCKED = "blocked"

class ClaimVerdict(str, Enum):
    VERIFIED = "verified"
    UNVERIFIED = "unverified"
    CONFLICTING = "conflicting"

class UserPlan(str, Enum):
    FREE = "free"
    PRO = "pro"