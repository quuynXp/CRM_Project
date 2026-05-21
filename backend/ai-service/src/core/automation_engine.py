import json
import logging
import re
from typing import List, Optional, Dict, Any
from pydantic import BaseModel
from google.generativeai import GenerativeModel
from src.core.cache import get_redis_client

logger = logging.getLogger(__name__)

class AutomationStep(BaseModel):
    step_id: int
    action_type: str  # NAVIGATE, API_CALL, UI_INTERACTION
    action_code: str  # key định danh hành động: OPEN_SETTINGS, CREATE_ROOM
    params: Dict[str, Any] = {}
    ui_description: str  # Text hiển thị trên UI
    voice_script: str  # Text để TTS đọc

class AutomationPlan(BaseModel):
    original_intent: str
    steps: List[AutomationStep]
    auto_execute: bool = False

SYSTEM_CAPABILITIES = """
Available Actions:
1. NAVIGATE:
   - code: GO_HOME, GO_SETTINGS, GO_PROFILE, GO_CHAT_ROOM
   - params: room_id (if GO_CHAT_ROOM)
2. API_CALL:
   - code: CREATE_ROOM (params: room_name, is_private)
   - code: TRANSLATE_TEXT (params: text, target_lang)
   - code: LOGOUT
3. UI_INTERACTION:
   - code: TOGGLE_MIC
   - code: TOGGLE_CAMERA
   - code: OPEN_KEYBOARD
"""

class AutomationEngine:
    def __init__(self):
        self.model = GenerativeModel(
            "gemini-2.5-flash",
            generation_config={"response_mime_type": "application/json", "temperature": 0.2}
        )

    async def plan_actions(self, user_text: str, context: Optional[str] = None) -> AutomationPlan:
        prompt = f"""
        You are an automation assistant for a chat/language app.
        User Intent: "{user_text}"
        Context: {context or "User is in lobby"}
        
        {SYSTEM_CAPABILITIES}
        
        Analyze the user intent and break it down into a sequence of steps.
        If the request is ambiguous, ask for clarification in 'voice_script'.
        
        Output format (JSON only):
        {{
            "steps": [
                {{
                    "step_id": 1,
                    "action_type": "NAVIGATE",
                    "action_code": "GO_SETTINGS",
                    "params": {{}},
                    "ui_description": "Mở màn hình cài đặt",
                    "voice_script": "Tôi sẽ mở cài đặt cho bạn ngay bây giờ."
                }}
            ],
            "auto_execute": true
        }}
        """
        
        try:
            response = await self.model.generate_content_async(prompt)
            raw_text = response.text.strip()
            # Clean markdown if present
            if raw_text.startswith("```json"):
                raw_text = raw_text[7:]
            if raw_text.endswith("```"):
                raw_text = raw_text[:-3]
            
            data = json.loads(raw_text)
            
            steps = []
            for item in data.get("steps", []):
                steps.append(AutomationStep(**item))
                
            return AutomationPlan(
                original_intent=user_text,
                steps=steps,
                auto_execute=data.get("auto_execute", False)
            )
            
        except Exception as e:
            logger.error(f"Automation planning failed: {e}")
            # Fallback plan
            return AutomationPlan(
                original_intent=user_text,
                steps=[
                    AutomationStep(
                        step_id=1,
                        action_type="UI_INTERACTION",
                        action_code="ERROR_FALLBACK",
                        ui_description="Không thể xử lý yêu cầu",
                        voice_script="Xin lỗi, tôi chưa hiểu yêu cầu này."
                    )
                ]
            )

automation_engine = AutomationEngine()