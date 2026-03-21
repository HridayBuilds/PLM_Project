"""
Smart ECO Impact Analysis Router
================================

Endpoints:
    POST /eco/impact/analyze       - Rule-based impact analysis (receives JSON input)
    POST /eco/impact/ai-insight    - AI-powered narrative insight using Groq API
    POST /eco/impact/batch-summary - Batch analysis of multiple ECOs

NO DATABASE CALLS - All data received via JSON from backend.

Author: PLM Engineering Team
"""

import os
import json
from typing import Optional
from datetime import datetime

import httpx
import uvicorn
from fastapi import APIRouter, HTTPException, status
from pydantic import BaseModel, ConfigDict, Field


# =============================================================================
# INPUT MODELS - What your backend guy needs to send
# =============================================================================

class ComponentProduct(BaseModel):
    """Component product info - needed for cost calculations"""
    model_config = ConfigDict(from_attributes=True)
    id: str
    name: str
    cost_price: float


class BOMChangeInput(BaseModel):
    """Single BOM change entry"""
    model_config = ConfigDict(from_attributes=True)
    id: str
    bom_component_id: str
    component_product: ComponentProduct  # The component's product info
    unit: str = "pcs"
    old_quantity: Optional[float] = None
    new_quantity: Optional[float] = None
    change_type: str  # "modified" | "added" | "removed"


class ProductChangeInput(BaseModel):
    """Single product field change"""
    model_config = ConfigDict(from_attributes=True)
    id: str
    field_name: str
    old_value: str
    new_value: str


class BOMOperationInput(BaseModel):
    """BOM operation for assembly time calculation"""
    model_config = ConfigDict(from_attributes=True)
    id: str
    operation_name: str
    work_center: Optional[str] = None
    expected_duration_minutes: float


class ECOInput(BaseModel):
    """
    Complete ECO data that backend needs to send.

    YOUR BACKEND GUY NEEDS TO SEND THIS JSON STRUCTURE:
    {
        "eco_id": "uuid-string",
        "eco_title": "Change motor specifications",
        "eco_type": "bom",  // "bom" or "product"
        "version_update": true,
        "status": "draft",  // "draft", "in_progress", "approved", "applied"
        "parent_product_cost_price": 150.00,  // baseline cost for % calculation
        "bom_changes": [
            {
                "id": "change-uuid",
                "bom_component_id": "component-uuid",
                "component_product": {
                    "id": "product-uuid",
                    "name": "Motor XYZ",
                    "cost_price": 25.50
                },
                "unit": "pcs",
                "old_quantity": 2,
                "new_quantity": 3,
                "change_type": "modified"
            }
        ],
        "product_changes": [
            {
                "id": "change-uuid",
                "field_name": "cost_price",
                "old_value": "100.00",
                "new_value": "120.00"
            }
        ],
        "bom_operations": [
            {
                "id": "op-uuid",
                "operation_name": "Assembly",
                "work_center": "Station A",
                "expected_duration_minutes": 30.0
            }
        ]
    }
    """
    eco_id: str
    eco_title: str
    eco_type: str = Field(..., pattern="^(bom|product)$")
    version_update: bool = False
    status: str = "draft"
    parent_product_cost_price: float  # Baseline cost for percentage calculation
    bom_changes: list[BOMChangeInput] = []
    product_changes: list[ProductChangeInput] = []
    bom_operations: list[BOMOperationInput] = []


class BatchECOInput(BaseModel):
    """Multiple ECOs for batch analysis"""
    ecos: list[ECOInput]


# =============================================================================
# OUTPUT MODELS - What the endpoints return
# =============================================================================

class DirectPriceChange(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    old: str
    new: str


class CostImpact(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    total_delta_amount: float
    cost_change_percent: float
    direct_price_change: Optional[DirectPriceChange] = None


class AssemblyTimeImpact(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    baseline_minutes: float
    delta_minutes: float
    new_total_minutes: float


class RiskAssessment(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    score: int
    level: str


class BOMChangeDetail(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    component_name: str
    unit: str
    old_quantity: Optional[float] = None
    new_quantity: Optional[float] = None
    change_type: str
    unit_cost: float
    cost_delta: float


class ProductChangeDetail(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    field_name: str
    old_value: str
    new_value: str


class ChangesSummary(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    bom_changes_count: int
    product_changes_count: int
    has_removals: bool
    has_additions: bool
    bom_changes: list[BOMChangeDetail]
    product_changes: list[ProductChangeDetail]


class ImpactAnalysisResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    eco_id: str
    eco_title: str
    eco_type: str
    version_update: bool
    cost_impact: CostImpact
    assembly_time_impact: AssemblyTimeImpact
    risk: RiskAssessment
    changes_summary: ChangesSummary


class AIInsight(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    risk_reasoning: str
    cost_reasoning: str
    recommendations: list[str]
    approval_advice: str


class AIInsightResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    eco_id: str
    risk_level: str
    cost_change_percent: float
    ai_insight: Optional[AIInsight] = None
    error_message: Optional[str] = None
    raw_analysis: dict


class ECOSummary(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    eco_id: str
    eco_title: str
    status: str
    risk_level: str
    risk_score: int
    cost_change_percent: float
    delta_minutes: float


class BatchSummaryResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    total_pending: int
    high_risk_count: int
    medium_risk_count: int
    low_risk_count: int
    eco_summaries: list[ECOSummary]


# =============================================================================
# Router
# =============================================================================

impact_router = APIRouter(tags=["ECO Impact Analysis"])


# =============================================================================
# GROQ API Configuration
# =============================================================================

GROQ_API_KEY = os.environ.get("GROQ_API_KEY")
if not GROQ_API_KEY:
    raise ValueError("GROQ_API_KEY environment variable is required. Please set it in your .env file.")
GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions"
GROQ_MODEL = "llama-3.3-70b-versatile"  # Fast and capable model


# =============================================================================
# Rule Engine Logic (Core Impact Analysis)
# =============================================================================

def compute_impact_analysis(eco_data: ECOInput) -> ImpactAnalysisResponse:
    """
    Core rule engine that computes impact analysis from JSON input.
    No database calls - all data comes from the input.
    """

    # Step 2: Calculate COST IMPACT
    total_cost_delta = 0.0
    bom_change_details: list[BOMChangeDetail] = []
    has_removals = False
    has_additions = False

    for change in eco_data.bom_changes:
        unit_cost = change.component_product.cost_price
        old_qty = change.old_quantity
        new_qty = change.new_quantity
        change_type = change.change_type

        if change_type == 'modified':
            delta = ((new_qty or 0) - (old_qty or 0)) * unit_cost
        elif change_type == 'added':
            delta = (new_qty or 0) * unit_cost
            has_additions = True
        elif change_type == 'removed':
            delta = -((old_qty or 0) * unit_cost)
            has_removals = True
        else:
            delta = 0.0

        total_cost_delta += delta

        bom_change_details.append(BOMChangeDetail(
            component_name=change.component_product.name,
            unit=change.unit,
            old_quantity=old_qty,
            new_quantity=new_qty,
            change_type=change_type,
            unit_cost=unit_cost,
            cost_delta=round(delta, 2)
        ))

    # Calculate cost change percent
    baseline_cost = eco_data.parent_product_cost_price or 1.0  # Avoid division by zero
    cost_change_percent = (total_cost_delta / baseline_cost) * 100 if baseline_cost else 0.0

    # Check for direct price change in product changes
    direct_price_change: Optional[DirectPriceChange] = None
    for pc in eco_data.product_changes:
        if pc.field_name == 'cost_price':
            direct_price_change = DirectPriceChange(
                old=pc.old_value,
                new=pc.new_value
            )
            break

    cost_impact = CostImpact(
        total_delta_amount=round(total_cost_delta, 2),
        cost_change_percent=round(cost_change_percent, 2),
        direct_price_change=direct_price_change
    )

    # Step 3: Calculate ASSEMBLY TIME IMPACT
    baseline_minutes = sum(op.expected_duration_minutes for op in eco_data.bom_operations)

    time_delta = 0.0
    for change in eco_data.bom_changes:
        old_qty = change.old_quantity or 0.0
        new_qty = change.new_quantity or 0.0
        qty_delta = new_qty - old_qty
        time_delta += qty_delta * 0.5  # Rule: 0.5 min per unit change

    assembly_time_impact = AssemblyTimeImpact(
        baseline_minutes=round(baseline_minutes, 2),
        delta_minutes=round(time_delta, 2),
        new_total_minutes=round(baseline_minutes + time_delta, 2)
    )

    # Step 4: Calculate RISK SCORE
    risk_score = 0
    risk_score += len(eco_data.bom_changes) * 10
    risk_score += len(eco_data.product_changes) * 8
    risk_score += 20 if eco_data.version_update else 0
    risk_score += 30 if has_removals else 0
    risk_score += 15 if has_additions else 0
    risk_score += 25 if abs(cost_change_percent) > 10 else 0

    if risk_score < 20:
        risk_level = 'Low'
    elif risk_score < 55:
        risk_level = 'Medium'
    else:
        risk_level = 'High'

    risk = RiskAssessment(score=risk_score, level=risk_level)

    # Build product changes details
    product_change_details = [
        ProductChangeDetail(
            field_name=pc.field_name,
            old_value=pc.old_value,
            new_value=pc.new_value
        )
        for pc in eco_data.product_changes
    ]

    changes_summary = ChangesSummary(
        bom_changes_count=len(eco_data.bom_changes),
        product_changes_count=len(eco_data.product_changes),
        has_removals=has_removals,
        has_additions=has_additions,
        bom_changes=bom_change_details,
        product_changes=product_change_details
    )

    # Step 5: Return response
    return ImpactAnalysisResponse(
        eco_id=eco_data.eco_id,
        eco_title=eco_data.eco_title,
        eco_type=eco_data.eco_type,
        version_update=eco_data.version_update,
        cost_impact=cost_impact,
        assembly_time_impact=assembly_time_impact,
        risk=risk,
        changes_summary=changes_summary
    )


# =============================================================================
# ENDPOINT 1: Rule-Based Impact Analysis
# =============================================================================

@impact_router.post(
    "/eco/impact/analyze",
    response_model=ImpactAnalysisResponse,
    summary="Rule-based impact analysis for a single ECO"
)
async def analyze_eco_impact(eco_data: ECOInput) -> ImpactAnalysisResponse:
    """
    Perform rule-based impact analysis on an Engineering Change Order (ECO).

    **Backend sends JSON, this endpoint calculates:**
    - Cost impact from BOM component changes
    - Assembly time impact
    - Risk score and level
    """
    return compute_impact_analysis(eco_data)


# =============================================================================
# ENDPOINT 2: AI Narrative (Groq LLM API)
# =============================================================================

@impact_router.post(
    "/eco/impact/ai-insight",
    response_model=AIInsightResponse,
    summary="AI-powered narrative insight using Groq LLM"
)
async def get_ai_insight(eco_data: ECOInput) -> AIInsightResponse:
    """
    Get AI-powered analysis and recommendations for an ECO.

    This endpoint:
    1. Runs the rule engine analysis on the input JSON
    2. Sends results to Groq API for narrative insight
    3. Returns combined analysis with AI recommendations
    """
    # Step 1: Run rule engine
    analysis = compute_impact_analysis(eco_data)
    raw_analysis = analysis.model_dump()

    # Step 2: Check for API key
    if not GROQ_API_KEY:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="GROQ_API_KEY environment variable is not set"
        )

    # Step 3: Build the prompt
    bom_changes_text = "\n".join([
        f"- {bc.component_name}: {bc.old_quantity} → {bc.new_quantity} ({bc.change_type}), unit cost: ${bc.unit_cost}"
        for bc in analysis.changes_summary.bom_changes
    ]) or "No BOM changes"

    product_changes_text = "\n".join([
        f"- Field '{pc.field_name}': {pc.old_value} → {pc.new_value}"
        for pc in analysis.changes_summary.product_changes
    ]) or "No product field changes"

    prompt = f"""You are a senior manufacturing analyst reviewing an Engineering Change Order in a PLM system.

ECO Title: {analysis.eco_title}
ECO Type: {analysis.eco_type} ECO
Version Update: {analysis.version_update}

Proposed Changes:
{bom_changes_text}
{product_changes_text}

Rule Engine Results:
- Cost change: {analysis.cost_impact.cost_change_percent}% (${analysis.cost_impact.total_delta_amount} absolute)
- Assembly time delta: {analysis.assembly_time_impact.delta_minutes} minutes (baseline: {analysis.assembly_time_impact.baseline_minutes} min)
- Risk level: {analysis.risk.level} (score: {analysis.risk.score}/100)
- Has removals: {analysis.changes_summary.has_removals}
- Has new additions: {analysis.changes_summary.has_additions}

Task: Return ONLY valid JSON (no markdown, no backticks, no explanation) in this exact shape:
{{
  "risk_reasoning": "2-3 sentences explaining WHY this risk level. Be specific about which changes drive it.",
  "cost_reasoning": "1-2 sentences on the cost impact and what drives it.",
  "recommendations": [
    "Specific actionable recommendation 1",
    "Specific actionable recommendation 2",
    "Specific actionable recommendation 3"
  ],
  "approval_advice": "One sentence: should approver scrutinize anything specific?"
}}"""

    # Step 4: Call Groq API
    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            response = await client.post(
                GROQ_API_URL,
                headers={
                    "Content-Type": "application/json",
                    "Authorization": f"Bearer {GROQ_API_KEY}"
                },
                json={
                    "model": GROQ_MODEL,
                    "messages": [{"role": "user", "content": prompt}],
                    "max_tokens": 500,
                    "temperature": 0.3  # Lower temp for more consistent JSON
                }
            )

            if response.status_code != 200:
                # AI unavailable, return rule engine result with error
                return AIInsightResponse(
                    eco_id=eco_data.eco_id,
                    risk_level=analysis.risk.level,
                    cost_change_percent=analysis.cost_impact.cost_change_percent,
                    ai_insight=None,
                    error_message=f"AI service unavailable: {response.status_code}",
                    raw_analysis=raw_analysis
                )

            response_data = response.json()

            # Extract text content from Groq response (OpenAI format)
            choices = response_data.get("choices", [])
            if not choices:
                return AIInsightResponse(
                    eco_id=eco_data.eco_id,
                    risk_level=analysis.risk.level,
                    cost_change_percent=analysis.cost_impact.cost_change_percent,
                    ai_insight=None,
                    error_message="AI service returned empty response",
                    raw_analysis=raw_analysis
                )

            ai_text = choices[0].get("message", {}).get("content", "")

            # Clean up potential markdown formatting
            ai_text = ai_text.strip()
            if ai_text.startswith("```json"):
                ai_text = ai_text[7:]
            if ai_text.startswith("```"):
                ai_text = ai_text[3:]
            if ai_text.endswith("```"):
                ai_text = ai_text[:-3]
            ai_text = ai_text.strip()

            # Parse JSON from AI response
            ai_json = json.loads(ai_text)

            ai_insight = AIInsight(
                risk_reasoning=ai_json.get("risk_reasoning", ""),
                cost_reasoning=ai_json.get("cost_reasoning", ""),
                recommendations=ai_json.get("recommendations", []),
                approval_advice=ai_json.get("approval_advice", "")
            )

            return AIInsightResponse(
                eco_id=eco_data.eco_id,
                risk_level=analysis.risk.level,
                cost_change_percent=analysis.cost_impact.cost_change_percent,
                ai_insight=ai_insight,
                error_message=None,
                raw_analysis=raw_analysis
            )

    except httpx.RequestError as e:
        return AIInsightResponse(
            eco_id=eco_data.eco_id,
            risk_level=analysis.risk.level,
            cost_change_percent=analysis.cost_impact.cost_change_percent,
            ai_insight=None,
            error_message=f"AI service unavailable: {str(e)}",
            raw_analysis=raw_analysis
        )
    except (json.JSONDecodeError, KeyError, IndexError) as e:
        return AIInsightResponse(
            eco_id=eco_data.eco_id,
            risk_level=analysis.risk.level,
            cost_change_percent=analysis.cost_impact.cost_change_percent,
            ai_insight=None,
            error_message=f"Failed to parse AI response: {str(e)}",
            raw_analysis=raw_analysis
        )


# =============================================================================
# ENDPOINT 3: Batch Analysis (Multiple ECOs)
# =============================================================================

@impact_router.post(
    "/eco/impact/batch-summary",
    response_model=BatchSummaryResponse,
    summary="Batch analysis of multiple ECOs"
)
async def get_batch_summary(batch_input: BatchECOInput) -> BatchSummaryResponse:
    """
    Analyze multiple ECOs at once. Backend sends list of ECOs,
    this endpoint runs rule engine on each (no AI call - too slow for batch).

    Returns sorted by risk_score descending.
    """
    eco_summaries: list[ECOSummary] = []
    high_risk_count = 0
    medium_risk_count = 0
    low_risk_count = 0

    for eco_data in batch_input.ecos:
        analysis = compute_impact_analysis(eco_data)

        summary = ECOSummary(
            eco_id=eco_data.eco_id,
            eco_title=eco_data.eco_title,
            status=eco_data.status,
            risk_level=analysis.risk.level,
            risk_score=analysis.risk.score,
            cost_change_percent=analysis.cost_impact.cost_change_percent,
            delta_minutes=analysis.assembly_time_impact.delta_minutes
        )
        eco_summaries.append(summary)

        # Count risk levels
        if analysis.risk.level == 'High':
            high_risk_count += 1
        elif analysis.risk.level == 'Medium':
            medium_risk_count += 1
        else:
            low_risk_count += 1

    # Sort by risk_score descending
    eco_summaries.sort(key=lambda x: x.risk_score, reverse=True)

    return BatchSummaryResponse(
        total_pending=len(eco_summaries),
        high_risk_count=high_risk_count,
        medium_risk_count=medium_risk_count,
        low_risk_count=low_risk_count,
        eco_summaries=eco_summaries
    )


# =============================================================================
# Main Entry Point
# =============================================================================

if __name__ == "__main__":
    from main import app
    uvicorn.run(app, host="0.0.0.0", port=8000, reload=True)
