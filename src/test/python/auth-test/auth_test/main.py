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


app = FastAPI(title="OAuth Login Service (Kakao & Apple)", version="1.0.0")

@app.get("/", response_class=HTMLResponse)
async def home(request: Request):
    """홈페이지 - 카카오/애플 로그인 버튼 및 로그인 정보 표시"""
    print("Set-Cookie headers:", request.headers.get("cookie"))

    # URL 파라미터에서 사용자 정보 추출
    user_id = request.query_params.get("id")
    nickname = request.query_params.get("nickname")
    profile_image = request.query_params.get("profileImage")
    is_new = request.query_params.get("isNew")
    provider_type = request.query_params.get("providerType")
    
    # auth code 추출 (새로운 플로우용)
    auth_code = request.query_params.get("code")
    
    # 사용자 정보가 있는지 확인
    has_user_info = any([user_id, nickname, profile_image])
    
    # 사용자 정보 섹션 HTML 생성
    user_info_section = ""
    if has_user_info:
        user_info_section = f"""
        <div class="user-info">
            <h2>로그인된 사용자</h2>
            <div class="user-details">
                {f'<p><strong>🆔 사용자 ID:</strong> <span style="background-color: #007bff; color: white; padding: 2px 8px; border-radius: 4px; font-family: monospace;">{user_id}</span></p>' if user_id else ''}
                {f'<p><strong>닉네임:</strong> {nickname}</p>' if nickname else ''}
                {f'<p><strong>프로필 이미지:</strong> <img src="{profile_image}" alt="프로필" style="width: 50px; height: 50px; border-radius: 50%;"></p>' if profile_image else ''}
                {f'<p><strong>신규 사용자:</strong> {is_new}</p>' if is_new else ''}
                {f'<p><strong>제공자:</strong> {provider_type}</p>' if provider_type else ''}
            </div>
        </div>
        <script>
            // 페이지 로드 시 사용자 ID를 입력 필드에 자동으로 채움
            document.addEventListener('DOMContentLoaded', function() {{
                const userIdInput = document.getElementById('userIdInput');
                if (userIdInput && '{user_id}') {{
                    userIdInput.value = '{user_id}';
                }}
            }});
        </script>
        """
    
    # auth code 섹션 HTML 생성
    auth_code_section = ""
    if auth_code:
        # provider_type을 URL에서 가져오거나 기본값 'kakao' 사용
        auth_code_section = f"""
        <div class="auth-code-info">
            <h2>받은 Auth Code</h2>
            <div class="code-display">{auth_code}</div>
            <p style="color: #666; font-size: 14px;">Provider: {provider_type.lower() or 'kakao'}</p>
            <button onclick="getTokenFromCode('{auth_code}', '{provider_type.lower() or 'kakao'}')" class="token-btn" style="margin-top: 15px;">2단계: Get Token</button>

            <div id="codeTokenInfo" class="token-info" style="display: none; margin-top: 15px;">
                <h3>Auth Code Flow 결과</h3>
                <div id="codeTokenDisplay"></div>
                <div id="userInfoFromToken" style="margin-top: 15px;"></div>
            </div>
        </div>
        """
    
    html_content = f"""
    <!DOCTYPE html>
    <html>
    <head>
        <title>OAuth 로그인 테스트</title>
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
            .apple-btn {{
                background-color: #000000;
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
            .apple-btn:hover {{ background-color: #333333; }}
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
            .auth-code-info {{
                margin-top: 30px;
                padding: 20px;
                background-color: #fff3cd;
                border-radius: 8px;
                border-left: 4px solid #ffc107;
                max-width: 600px;
                margin-left: auto;
                margin-right: auto;
            }}
            .code-display {{
                word-break: break-all;
                font-family: monospace;
                background-color: #f8f9fa;
                padding: 15px;
                border-radius: 4px;
                margin: 15px 0;
                border: 1px solid #ddd;
            }}
            .auth-flow-section {{
                margin-top: 30px;
                padding: 20px;
                background-color: #e8f5e8;
                border-radius: 8px;
                border-left: 4px solid #28a745;
                max-width: 600px;
                margin-left: auto;
                margin-right: auto;
            }}
            .success-btn {{
                background-color: #28a745; 
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
            .success-btn:hover {{ background-color: #218838; }}
            .profile-section {{
                margin-top: 30px;
                padding: 20px;
                background-color: #f0f8ff;
                border-radius: 8px;
                border-left: 4px solid #007bff;
                max-width: 600px;
                margin-left: auto;
                margin-right: auto;
            }}
        </style>
    </head>
    <body>
        <h1>OAuth 로그인 테스트</h1>
        <p>아래 버튼을 클릭하여 로그인하세요</p>

        <div style="margin: 20px 0;">
            <h3>토큰 직접 발급 방식</h3>
            <p style="margin-bottom: 15px; color: #666;">인증 후 바로 토큰을 발급받아 쿠키로 설정</p>
            <button onclick="loginWithResponseType('kakao')" class="login-btn">카카오로 로그인 (토큰 방식)</button>
            <button onclick="loginWithResponseType('apple')" class="apple-btn">🍎 Apple로 로그인 (토큰 방식)</button>
            
            <div class="token-section" style="margin-top: 20px;">
                <div class="token-input-group">
                    <button onclick="getToken()" class="token-btn">Get Access Token</button>
                </div>
                <p style="margin-top: 10px; color: #666; font-size: 14px;">
                    쿠키의 refresh token으로 access token을 발급받습니다.
                </p>
                
                <div id="tokenInfo" class="token-info" style="display: none; margin-top: 15px;">
                    <h3>Access Token</h3>
                    <div id="tokenDisplay" class="token-display"></div>
                </div>
            </div>
        </div>
        
        <div class="auth-flow-section">
            <h3>Auth Code Flow 방식</h3>
            <p style="margin-bottom: 15px; color: #666;">1단계: Auth Code 받기 → 2단계: Code로 토큰 발급</p>
            <div style="margin: 15px 0;">
                <button onclick="loginWithResponseType('kakao', 'code')" class="success-btn">1단계: Kakao Auth Code</button>
                <button onclick="loginWithResponseType('apple', 'code')" class="success-btn" style="background-color: #000; margin-left: 10px;">1단계: 🍎 Apple Auth Code</button>
                <p style="margin: 10px 0; font-size: 14px; color: #666;">
                    💡 responseType=code로 설정하면 auth code만 받습니다
                </p>
            </div>
        </div>
        {user_info_section}
        {auth_code_section}
        
        <div class="profile-section">
            <h3>프로필 조회 (공통)</h3>
            <p style="margin-bottom: 15px; color: #666;">Access Token이 있으면 언제든지 프로필을 조회할 수 있습니다</p>
            
            <div class="token-section">
                <div style="margin: 15px 0;">
                    <button onclick="getMyProfile()" class="token-btn" style="margin-right: 10px;">Get My Profile (/me)</button>
                    <input type="number" id="userIdInput" placeholder="사용자 ID 입력" class="token-input" style="width: 200px; margin-right: 10px;">
                    <button onclick="getProfile()" class="token-btn">Get Profile (/{id})</button>
                </div>
                
                <div id="myProfileInfo" class="token-info" style="display: none; margin-top: 15px;">
                    <h4>내 프로필 (/me)</h4>
                    <div id="myProfileDisplay"></div>
                </div>
                
                <div id="profileInfo" class="token-info" style="display: none; margin-top: 15px;">
                    <h4>사용자 프로필 (/{id})</h4>
                    <div id="profileDisplay"></div>
                </div>
            </div>
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
            
            function loginWithResponseType(provider, responseType) {{
                // provider가 responseType일 수 있음 (하위 호환성)
                if (!responseType && (provider === 'code' || !provider)) {{
                    responseType = provider;
                    provider = 'kakao';
                }}

                // 기본값 설정
                provider = provider || 'kakao';

                // 선택한 provider와 responseType으로 로그인 시작
                const redirectUri = encodeURIComponent('http://localhost:{SERVICE_PORT}');
                let loginUrl = `{SERVER_URL}/api/v1/auth/${{provider}}/login?redirectUri=${{redirectUri}}`;

                // responseType이 있는 경우에만 추가
                if (responseType) {{
                    loginUrl += `&responseType=${{responseType}}`;
                }}

                console.log('🔐 Login URL:', loginUrl, 'Provider:', provider, 'ResponseType:', responseType);

                // POST 요청을 위한 form 생성 및 제출
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = loginUrl;
                form.target = '_self';
                document.body.appendChild(form);
                form.submit();
            }}
            
            async function getTokenFromCode(code, provider) {{
                try {{
                    // provider 기본값 설정 (URL에서 provider 타입 추론)
                    if (!provider) {{
                        const urlParams = new URLSearchParams(window.location.search);
                        provider = urlParams.get('providerType') || 'kakao';
                    }}

                    const response = await fetch(`{SERVER_URL}/api/v1/auth/${{provider}}/token`, {{
                        method: 'POST',
                        headers: {{
                            'Content-Type': 'application/json',
                        }},
                        body: JSON.stringify({{
                            code: code
                        }})
                    }});
                    
                    if (response.ok) {{
                        const data = await response.json();
                        
                        // 토큰 정보 표시
                        const tokenInfoHtml = `
                            <p><strong>Access Token:</strong></p>
                            <div style="word-break: break-all; font-family: monospace; background-color: #f8f9fa; padding: 10px; border-radius: 4px; margin: 10px 0;">
                                ${{data.accessToken || 'No access token'}}
                            </div>
                            <p><strong>Refresh Token:</strong></p>
                            <div style="word-break: break-all; font-family: monospace; background-color: #f8f9fa; padding: 10px; border-radius: 4px; margin: 10px 0;">
                                ${{data.refreshToken || 'No refresh token'}}
                            </div>
                        `;
                        document.getElementById('codeTokenDisplay').innerHTML = tokenInfoHtml;
                        
                        // 사용자 정보 표시
                        const userInfoHtml = `
                            <h4>사용자 정보:</h4>
                            <p><strong>ID:</strong> ${{data.id || 'N/A'}}</p>
                            <p><strong>닉네임:</strong> ${{data.nickname || 'N/A'}}</p>
                            <p><strong>프로필 이미지:</strong> ${{data.profileImage ? `<img src="${{data.profileImage}}" style="width: 30px; height: 30px; border-radius: 50%;">` : 'N/A'}}</p>
                            <p><strong>신규 사용자:</strong> ${{data.isNew || 'N/A'}}</p>
                            <p><strong>제공자:</strong> ${{data.socialProvider?.type || 'N/A'}}</p>
                        `;
                        document.getElementById('userInfoFromToken').innerHTML = userInfoHtml;
                        document.getElementById('codeTokenInfo').style.display = 'block';
                    }} else {{
                        const errorData = await response.json();
                        alert('토큰 발급 실패: ' + (errorData.message || 'Unknown error'));
                    }}
                }} catch (error) {{
                    alert('토큰 발급 중 오류 발생: ' + error.message);
                }}
            }}
            
            async function getProfile() {{
                try {{
                    // Token 방식의 Access Token 또는 Auth Code Flow의 Access Token 찾기
                    let accessToken = null;
                    
                    // 1. Token 방식에서 발급받은 Access Token 확인
                    const tokenDisplay = document.getElementById('tokenDisplay');
                    if (tokenDisplay && tokenDisplay.textContent && tokenDisplay.textContent !== 'No token received') {{
                        accessToken = tokenDisplay.textContent;
                    }}
                    
                    // 2. Auth Code Flow에서 발급받은 Access Token 확인 (codeTokenDisplay에서 추출)
                    if (!accessToken) {{
                        const codeTokenDisplay = document.getElementById('codeTokenDisplay');
                        if (codeTokenDisplay) {{
                            const tokenMatch = codeTokenDisplay.innerHTML.match(/Access Token:<\/strong><\/p>[\s\S]*?<div[^>]*>([^<]+)<\/div>/);
                            if (tokenMatch && tokenMatch[1] && tokenMatch[1] !== 'No access token') {{
                                accessToken = tokenMatch[1].trim();
                            }}
                        }}
                    }}
                    
                    if (!accessToken) {{
                        alert('Access Token이 없습니다. 먼저 로그인하거나 토큰을 발급받아주세요.');
                        return;
                    }}
                    
                    const userId = document.getElementById('userIdInput').value;
                    if (!userId) {{
                        alert('사용자 ID를 입력해주세요.');
                        return;
                    }}
                    
                    const response = await fetch(`{SERVER_URL}/api/v1/users/${{userId}}`, {{
                        method: 'GET',
                        headers: {{
                            'Authorization': `Bearer ${{accessToken}}`,
                            'Content-Type': 'application/json',
                        }}
                    }});
                    
                    if (response.ok) {{
                        const data = await response.json();
                        const profileData = data.data; // SuccessResponse의 data 필드
                        
                        const profileHtml = `
                            <p><strong>ID:</strong> ${{profileData.id}}</p>
                            <p><strong>이메일:</strong> ${{profileData.email}}</p>
                            <p><strong>닉네임:</strong> ${{profileData.nickname}}</p>
                            <p><strong>프로필 이미지:</strong> ${{profileData.profileImage ? `<img src="${{profileData.profileImage}}" style="width: 50px; height: 50px; border-radius: 50%;">` : 'N/A'}}</p>
                            <p><strong>성별:</strong> ${{profileData.gender || 'N/A'}}</p>
                            <p><strong>출생연도:</strong> ${{profileData.birthYear || 'N/A'}}</p>
                        `;
                        document.getElementById('profileDisplay').innerHTML = profileHtml;
                        document.getElementById('profileInfo').style.display = 'block';
                    }} else {{
                        const errorData = await response.json();
                        let errorMessage = 'Unknown error';
                        if (response.status === 403) {{
                            errorMessage = '권한이 없습니다. 본인의 ID만 조회할 수 있습니다.';
                        }} else if (errorData.message) {{
                            errorMessage = errorData.message;
                        }}
                        alert('프로필 조회 실패: ' + errorMessage);
                    }}
                }} catch (error) {{
                    alert('프로필 조회 중 오류 발생: ' + error.message);
                }}
            }}
            
            async function getMyProfile() {{
                try {{
                    // Token 방식의 Access Token 또는 Auth Code Flow의 Access Token 찾기
                    let accessToken = null;
                    
                    // 1. Token 방식에서 발급받은 Access Token 확인
                    const tokenDisplay = document.getElementById('tokenDisplay');
                    if (tokenDisplay && tokenDisplay.textContent && tokenDisplay.textContent !== 'No token received') {{
                        accessToken = tokenDisplay.textContent;
                    }}
                    
                    // 2. Auth Code Flow에서 발급받은 Access Token 확인 (codeTokenDisplay에서 추출)
                    if (!accessToken) {{
                        const codeTokenDisplay = document.getElementById('codeTokenDisplay');
                        if (codeTokenDisplay) {{
                            const tokenMatch = codeTokenDisplay.innerHTML.match(/Access Token:<\/strong><\/p>[\s\S]*?<div[^>]*>([^<]+)<\/div>/);
                            if (tokenMatch && tokenMatch[1] && tokenMatch[1] !== 'No access token') {{
                                accessToken = tokenMatch[1].trim();
                            }}
                        }}
                    }}
                    
                    if (!accessToken) {{
                        alert('Access Token이 없습니다. 먼저 로그인하거나 토큰을 발급받아주세요.');
                        return;
                    }}
                    
                    const response = await fetch(`{SERVER_URL}/api/v1/users/me`, {{
                        method: 'GET',
                        headers: {{
                            'Authorization': `Bearer ${{accessToken}}`,
                            'Content-Type': 'application/json',
                        }}
                    }});
                    
                    if (response.ok) {{
                        const data = await response.json();
                        const profileData = data.data; // SuccessResponse의 data 필드
                        
                        const profileHtml = `
                            <p><strong>ID:</strong> ${{profileData.id}}</p>
                            <p><strong>이메일:</strong> ${{profileData.email}}</p>
                            <p><strong>닉네임:</strong> ${{profileData.nickname}}</p>
                            <p><strong>프로필 이미지:</strong> ${{profileData.profileImage ? `<img src="${{profileData.profileImage}}" style="width: 50px; height: 50px; border-radius: 50%;">` : 'N/A'}}</p>
                            <p><strong>성별:</strong> ${{profileData.gender || 'N/A'}}</p>
                            <p><strong>출생연도:</strong> ${{profileData.birthYear || 'N/A'}}</p>
                        `;
                        document.getElementById('myProfileDisplay').innerHTML = profileHtml;
                        document.getElementById('myProfileInfo').style.display = 'block';
                        
                        // 자동으로 사용자 ID 입력 필드에 채우기
                        const userIdInput = document.getElementById('userIdInput');
                        if (userIdInput && profileData.id) {{
                            userIdInput.value = profileData.id;
                        }}
                    }} else {{
                        const errorData = await response.json();
                        let errorMessage = 'Unknown error';
                        if (response.status === 401) {{
                            errorMessage = '인증이 필요합니다. 유효한 Access Token을 사용해주세요.';
                        }} else if (errorData.message) {{
                            errorMessage = errorData.message;
                        }}
                        alert('내 프로필 조회 실패: ' + errorMessage);
                    }}
                }} catch (error) {{
                    alert('내 프로필 조회 중 오류 발생: ' + error.message);
                }}
            }}
            
        </script>
    </body>
    </html>
    """
    return HTMLResponse(content=html_content)


if __name__ == "__main__":
    SERVICE_PORT = int(os.environ.get("SERVICE_PORT", 3000))
    SERVER_URL = os.environ.get("SERVER_URL", "http://localhost:8080")
    print("🚀 카카오 OAuth 로그인 서비스 시작 중...")
    print("SERVER_URL:", SERVER_URL)

    uvicorn.run(
        app,
        host="0.0.0.0",
        port=SERVICE_PORT,
        # reload=True
    )
