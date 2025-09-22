from fastapi import FastAPI, HTTPException, Request, Depends
from fastapi.responses import HTMLResponse, RedirectResponse
from fastapi.security import HTTPBearer
import httpx
import uvicorn
from typing import Optional
import os
from datetime import datetime, timedelta
import jwt
from jwt import PyJWKClient
from pydantic import BaseModel
import uuid
import json


app = FastAPI(title="Kakao OAuth Login Service", version="1.0.0")
SERVER_URL = os.environ.get("SERVER_URL", "http://211.188.58.167")
# SERVER_URL = "http://localhost:8080"

@app.get("/", response_class=HTMLResponse)
async def home(request: Request):
    """홈페이지 - 카카오 로그인 버튼 및 로그인 정보 표시"""
    print("Set-Cookie headers:", request.headers.get("cookie"))

    # URL 파라미터에서 사용자 정보 추출
    user_id = request.query_params.get("id")
    nickname = request.query_params.get("nickname")
    profile_image = request.query_params.get("profileImage")
    is_new = request.query_params.get("isNew")
    provider_type = request.query_params.get("providerType")
    
    # 사용자 정보가 있는지 확인
    has_user_info = any([user_id, nickname, profile_image])
    
    # 사용자 정보 섹션 HTML 생성
    user_info_section = ""
    if has_user_info:
        user_info_section = f"""
        <div class="user-info">
            <h2>로그인된 사용자</h2>
            <div class="user-details">
                {f'<p><strong>ID:</strong> {user_id}</p>' if user_id else ''}
                {f'<p><strong>닉네임:</strong> {nickname}</p>' if nickname else ''}
                {f'<p><strong>프로필 이미지:</strong> <img src="{profile_image}" alt="프로필" style="width: 50px; height: 50px; border-radius: 50%;"></p>' if profile_image else ''}
                {f'<p><strong>신규 사용자:</strong> {is_new}</p>' if is_new else ''}
                {f'<p><strong>제공자:</strong> {provider_type}</p>' if provider_type else ''}
            </div>
        </div>
        """
    
    html_content = f"""
    <!DOCTYPE html>
    <html>
    <head>
        <title>카카오 로그인 테스트</title>
        <meta charset="utf-8">
        <style>
            body {{ font-family: Arial, sans-serif; text-align: center; margin-top: 50px; }}
            .login-btn {{ 
                background-color: #FEE500; 
                color: #000; 
                padding: 15px 30px; 
                border: none; 
                border-radius: 8px; 
                font-size: 16px; 
                cursor: pointer; 
                text-decoration: none;
                display: inline-block;
                margin: 10px;
            }}
            .login-btn:hover {{ background-color: #FFD700; }}
            .token-btn {{
                background-color: #007bff; 
                color: #fff; 
                padding: 15px 30px; 
                border: none; 
                border-radius: 8px; 
                font-size: 16px; 
                cursor: pointer; 
                text-decoration: none;
                display: inline-block;
                margin: 10px;
            }}
            .token-btn:hover {{ background-color: #0056b3; }}
            .token-section {{
                margin-top: 20px;
                padding: 20px;
                background-color: #f8f9fa;
                border-radius: 8px;
                max-width: 600px;
                margin-left: auto;
                margin-right: auto;
            }}
            .token-input-group {{
                display: flex;
                gap: 10px;
                align-items: center;
                justify-content: center;
                flex-wrap: wrap;
            }}
            .token-input {{
                flex: 1;
                min-width: 300px;
                padding: 12px 15px;
                border: 2px solid #ddd;
                border-radius: 8px;
                font-size: 14px;
                font-family: monospace;
            }}
            .token-input:focus {{
                outline: none;
                border-color: #007bff;
                box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
            }}
            .user-info {{
                margin-top: 30px;
                padding: 20px;
                background-color: #f5f5f5;
                border-radius: 8px;
                max-width: 500px;
                margin-left: auto;
                margin-right: auto;
                text-align: left;
            }}
            .user-details p {{
                margin: 10px 0;
            }}
            .token-info {{
                margin-top: 20px;
                padding: 15px;
                background-color: #e8f4fd;
                border-radius: 8px;
                border-left: 4px solid #007bff;
            }}
            .token-display {{
                word-break: break-all;
                font-family: monospace;
                background-color: #f8f9fa;
                padding: 10px;
                border-radius: 4px;
                margin-top: 10px;
            }}
        </style>
    </head>
    <body>
        <h1>카카오 OAuth 로그인 테스트</h1>
        <p>아래 버튼을 클릭하여 카카오로 로그인하세요</p>
        <form action="{SERVER_URL}/api/v1/auth/kakao/login?redirectUri=http://localhost:{SERVICE_PORT}" method="post" style="display:inline;">
            <button type="submit" class="login-btn">카카오로 로그인</button>
        </form>
        
        <div class="token-section">
            <div class="token-input-group">
                <button onclick="getToken()" class="token-btn">Get token</button>
            </div>
            <p style="margin-top: 10px; color: #666; font-size: 14px;">
                Refresh token은 쿠키에서 자동으로 가져옵니다.
            </p>
        </div>
        {user_info_section}
        
        <div id="tokenInfo" class="token-info" style="display: none;">
            <h3>Access Token</h3>
            <div id="tokenDisplay" class="token-display"></div>
        </div>
        
        <script>
            async function getToken() {{
                try {{
                    const response = await fetch('{SERVER_URL}/api/v1/auth/refresh', {{
                        method: 'POST',
                        headers: {{
                            'Content-Type': 'application/json',
                        }},
                        credentials: 'include'  // 쿠키를 포함하여 요청
                    }});
                    
                    if (response.ok) {{
                        const data = await response.json();
                        document.getElementById('tokenDisplay').textContent = data.accessToken || 'No token received';
                        document.getElementById('tokenInfo').style.display = 'block';
                    }} else {{
                        const errorData = await response.json();
                        alert('토큰 가져오기 실패: ' + (errorData.message || 'Unknown error'));
                    }}
                }} catch (error) {{
                    alert('토큰 가져오기 중 오류 발생: ' + error.message);
                }}
            }}
        </script>
    </body>
    </html>
    """
    return HTMLResponse(content=html_content)


SERVICE_PORT = int(os.environ.get("SERVICE_PORT", 3000))

if __name__ == "__main__":
    print("🚀 카카오 OAuth 로그인 서비스 시작 중...")
    print(f"SERVER_URL: {SERVER_URL}")
    uvicorn.run(
        app,
        host="0.0.0.0",
        port=SERVICE_PORT,
        # reload=True
    )
