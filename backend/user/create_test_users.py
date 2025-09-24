#!/usr/bin/env python3
"""
실명인증 서버 테스트 사용자 생성 스크립트
"""

import requests
import json
import time

# 실명인증 서버 URL
BASE_URL = "http://localhost:8084"

# 테스트 사용자 데이터
test_users = [
    {
        "name": "김상담",
        "birthDate": "19900101",
        "gender": "M",
        "residentNumber": "9001011234567",
        "phone": "010-1234-5678"
    },
    {
        "name": "김영희",
        "birthDate": "19950515",
        "gender": "F",
        "residentNumber": "9505152234567",
        "phone": "010-2345-6789"
    },
    {
        "name": "박민수",
        "birthDate": "20001225",
        "gender": "M",
        "residentNumber": "0012253234567",
        "phone": "010-3456-7890"
    },
    {
        "name": "이지은",
        "birthDate": "20050310",
        "gender": "F",
        "residentNumber": "0503104234567",
        "phone": "010-4567-8901"
    }
]

def create_user(user_data):
    """사용자 생성"""
    url = f"{BASE_URL}/api/user"
    
    payload = {
        "name": user_data["name"],
        "birthDate": user_data["birthDate"],
        "gender": user_data["gender"],
        "residentNumber": user_data["residentNumber"],
        "phone": user_data["phone"]
    }
    
    headers = {
        "Content-Type": "application/json"
    }
    
    # 디버깅: 전송할 데이터 출력
    print(f"   📤 전송 데이터: {json.dumps(payload, ensure_ascii=False, indent=2)}")
    
    try:
        response = requests.post(url, json=payload, headers=headers)
        
        if response.status_code == 200 or response.status_code == 201:
            result = response.json()
            print(f"✅ 사용자 생성 성공: {user_data['name']}")
            print(f"   - 사용자 ID: {result.get('userId', 'N/A')}")
            print(f"   - CI: {result.get('ci', 'N/A')}")
            print(f"   - 전화번호: {result.get('phone', 'N/A')}")
            return True
        else:
            print(f"❌ 사용자 생성 실패: {user_data['name']}")
            print(f"   - 상태 코드: {response.status_code}")
            print(f"   - 응답: {response.text}")
            return False
            
    except requests.exceptions.RequestException as e:
        print(f"❌ 요청 오류: {user_data['name']} - {str(e)}")
        return False

def verify_ci(user_data):
    """CI 검증 테스트"""
    url = f"{BASE_URL}/api/user/ci/verify"
    
    payload = {
        "name": user_data["name"],
        "birthDate": user_data["birthDate"],
        "gender": user_data["gender"],
        "residentNumber": user_data["residentNumber"]
    }
    
    headers = {
        "Content-Type": "application/json"
    }
    
    try:
        response = requests.post(url, json=payload, headers=headers)
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ CI 검증 성공: {user_data['name']}")
            print(f"   - 검증 결과: {result.get('verified', False)}")
            print(f"   - CI: {result.get('ci', 'N/A')}")
            return True
        else:
            print(f"❌ CI 검증 실패: {user_data['name']}")
            print(f"   - 상태 코드: {response.status_code}")
            print(f"   - 응답: {response.text}")
            return False
            
    except requests.exceptions.RequestException as e:
        print(f"❌ CI 검증 요청 오류: {user_data['name']} - {str(e)}")
        return False

def convert_ci(user_data):
    """CI 변환 테스트"""
    url = f"{BASE_URL}/api/user/ci/convert"
    
    payload = {
        "name": user_data["name"],
        "birthDate": user_data["birthDate"],
        "gender": user_data["gender"],
        "residentNumber": user_data["residentNumber"]
    }
    
    headers = {
        "Content-Type": "application/json"
    }
    
    try:
        response = requests.post(url, json=payload, headers=headers)
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ CI 변환 성공: {user_data['name']}")
            print(f"   - CI: {result.get('ci', 'N/A')}")
            return result.get('ci')
        else:
            print(f"❌ CI 변환 실패: {user_data['name']}")
            print(f"   - 상태 코드: {response.status_code}")
            print(f"   - 응답: {response.text}")
            return None
            
    except requests.exceptions.RequestException as e:
        print(f"❌ CI 변환 요청 오류: {user_data['name']} - {str(e)}")
        return None

def main():
    """메인 실행 함수"""
    print("🚀 실명인증 서버 테스트 사용자 생성 시작")
    print("=" * 60)
    
    success_count = 0
    total_count = len(test_users)
    
    for i, user_data in enumerate(test_users, 1):
        print(f"\n[{i}/{total_count}] 사용자 생성 중: {user_data['name']}")
        print("-" * 40)
        
        # 1. CI 변환 테스트
        print("1. CI 변환 테스트...")
        ci = convert_ci(user_data)
        
        if ci:
            # 2. CI 검증 테스트
            print("2. CI 검증 테스트...")
            verify_ci(user_data)
            
            # 3. 사용자 생성
            print("3. 사용자 생성...")
            if create_user(user_data):
                success_count += 1
        
        # 요청 간 간격 (서버 부하 방지)
        time.sleep(1)
    
    print("\n" + "=" * 60)
    print(f"📊 생성 완료: {success_count}/{total_count} 사용자")
    
    if success_count == total_count:
        print("🎉 모든 사용자가 성공적으로 생성되었습니다!")
    else:
        print("⚠️  일부 사용자 생성에 실패했습니다.")

if __name__ == "__main__":
    main()
