import base64
import logging
from fastapi import APIRouter, Depends, HTTPException, BackgroundTasks
from pydantic import BaseModel
from redis.asyncio import Redis

from src.core.session import get_db
from src.core.cache import get_redis_client
from src.core.automation_engine import automation_engine, AutomationPlan
from src.api.tts_generator import generate_tts
from src.auth.auth_utils import security

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/automation", tags=["Automation"])

class PlanRequest(BaseModel):
    text: str
    current_screen: str = "home"

class StepAudioRequest(BaseModel):
    text: str
    voice_lang: str = "vi"

@router.post("/plan", response_model=AutomationPlan)
async def create_action_plan(
    request: PlanRequest,
    user=Depends(security)  # Yêu cầu login
):
    """
    Phân tích text người dùng -> Trả về chuỗi JSON actions.
    Frontend sẽ loop qua list này để thực thi hoặc hiển thị UI.
    """
    try:
        plan = await automation_engine.plan_actions(request.text, context=f"Screen: {request.current_screen}")
        return plan
    except Exception as e:
        logger.error(f"Plan Error: {e}")
        raise HTTPException(status_code=500, detail="Could not generate action plan")

@router.post("/step-audio")
async def generate_step_audio(
    request: StepAudioRequest,
    redis: Redis = Depends(get_redis_client)
):
    """
    Lazy loading audio cho từng bước. 
    Frontend gọi API này cho mỗi step.voice_script để lấy âm thanh.
    """
    try:
        audio_bytes, error = await generate_tts(request.text, request.voice_lang, redis)
        if error:
            raise HTTPException(status_code=500, detail=error)
        
        return {
            "audio_base64": base64.b64encode(audio_bytes).decode('utf-8'),
            "text": request.text
        }
    except Exception as e:
        logger.error(f"Audio Gen Error: {e}")
        raise HTTPException(status_code=500, detail="TTS Failed")