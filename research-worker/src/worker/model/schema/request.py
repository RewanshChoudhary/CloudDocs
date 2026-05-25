from typing import Annotated

from pydantic import BaseModel, Field
from .enums import Domain, OutputFormat


class ResearchRequest(BaseModel):
    query: str = Field(
        min_length=5,
        max_length=200,
        description="User query to provide the LLM"
    )

    domain: Domain = Field(
        default=Domain.GENERAL,
        description="Domain of the request"
    )

    output_format: OutputFormat = Field(
        default=OutputFormat.JSON,
        description="The output format of the response required"
    )

    max_sources: Annotated[int, Field(ge=1, le=20)] = Field(
        default=5,
        description="Maximum number of sources to process"
    )
    sources: list[str] | None =Field(
        description="The allowed sources given to the backend can be null",
        default="wikipedia.com")
         
    banned_sources: list[str] | None =Field (
        description="The sources which the system cannot use",
    )
