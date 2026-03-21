"""
Smart ECO Impact Analysis - Main Application
=============================================

FastAPI application for PLM (Product Lifecycle Management) system's
Smart ECO Impact Analysis feature.

NO DATABASE - Receives JSON from backend, processes, and returns results.

Run with: uvicorn main:app --reload
"""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from eco_impact_router import impact_router

# =============================================================================
# FastAPI Application Setup
# =============================================================================

app = FastAPI(
    title="Smart ECO Impact Analysis API",
    description="""
    PLM (Product Lifecycle Management) system's Smart ECO Impact Analysis feature.

    **This API receives JSON from your backend and provides:**
    - Rule-based impact analysis for Engineering Change Orders (ECOs)
    - AI-powered narrative insights using Groq LLM
    - Batch analysis of multiple ECOs

    **No database connections** - all data comes via JSON input.
    """,
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# =============================================================================
# CORS Middleware Configuration
# =============================================================================

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Configure appropriately for production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# =============================================================================
# Include Routers
# =============================================================================

app.include_router(impact_router, prefix="/api/v1")

# =============================================================================
# Health Check Endpoint
# =============================================================================


@app.get("/health", tags=["Health"])
async def health_check():
    """Health check endpoint for monitoring."""
    return {"status": "healthy", "service": "smart-eco-impact-analysis"}


@app.get("/", tags=["Root"])
async def root():
    """Root endpoint with API information."""
    return {
        "message": "Smart ECO Impact Analysis API",
        "version": "1.0.0",
        "docs": "/docs",
        "endpoints": {
            "analyze": "POST /api/v1/eco/impact/analyze",
            "ai_insight": "POST /api/v1/eco/impact/ai-insight",
            "batch_summary": "POST /api/v1/eco/impact/batch-summary"
        }
    }


# =============================================================================
# Main Entry Point
# =============================================================================

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
