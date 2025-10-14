#!/usr/bin/env python3
"""
HANAinPLAN 통합 테스트 데이터 삽입 스크립트

User 서버에서 CI 발급 → 각 은행에 고객 등록 → 계좌 생성
추가로 hanainplan 서버에 업종코드/질병코드 데이터 삽입
"""

import requests
import json
import time
import random
from datetime import datetime, timedelta
from decimal import Decimal

# ============================================================================
# 서버 설정
# ============================================================================

USER_SERVER = "http://localhost:8084"
HANAINPLAN_SERVER = "http://localhost:8080"

BANK_CONFIGS = {
    "hana": {
        "name": "하나은행",
        "url": "http://localhost:8081",
        "api_prefix": "/api/hana"
    },
    "shinhan": {
        "name": "신한은행",
        "url": "http://localhost:8082",
        "api_prefix": "/api/shinhan"
    },
    "kookmin": {
        "name": "국민은행",
        "url": "http://localhost:8083",
        "api_prefix": "/api/kookmin"
    }
}

# ============================================================================
# 테스트 사용자 데이터 (10명)
# ============================================================================

TEST_USERS = [
    {
        "name": "주소영",
        "birthDate": "20010919",
        "gender": "F",
        "residentNumber": "0109194201214",
        "phone": "010-8965-0136"
    },
    {
        "name": "김민준",
        "birthDate": "19900315",
        "gender": "M",
        "residentNumber": "9003151234567",
        "phone": "010-1234-5001"
    },
    {
        "name": "이서연",
        "birthDate": "19950820",
        "gender": "F",
        "residentNumber": "9508202234567",
        "phone": "010-1234-5002"
    },
    {
        "name": "박지훈",
        "birthDate": "19880705",
        "gender": "M",
        "residentNumber": "8807051234567",
        "phone": "010-1234-5003"
    },
    {
        "name": "최수민",
        "birthDate": "19921201",
        "gender": "F",
        "residentNumber": "9212012234567",
        "phone": "010-1234-5004"
    },
    {
        "name": "정현우",
        "birthDate": "19850410",
        "gender": "M",
        "residentNumber": "8504101234567",
        "phone": "010-1234-5005"
    },
    {
        "name": "강지은",
        "birthDate": "19980625",
        "gender": "F",
        "residentNumber": "9806252234567",
        "phone": "010-1234-5006"
    },
    {
        "name": "조성훈",
        "birthDate": "19931118",
        "gender": "M",
        "residentNumber": "9311181234567",
        "phone": "010-1234-5007"
    },
    {
        "name": "윤아영",
        "birthDate": "19870222",
        "gender": "F",
        "residentNumber": "8702222234567",
        "phone": "010-1234-5008"
    },
    {
        "name": "임동혁",
        "birthDate": "19960908",
        "gender": "M",
        "residentNumber": "9609083234567",
        "phone": "010-1234-5009"
    },
    {
        "name": "한예린",
        "birthDate": "19940514",
        "gender": "F",
        "residentNumber": "9405144234567",
        "phone": "010-1234-5010"
    }
]

# 계좌 유형 정의
ACCOUNT_TYPES = {
    0: {"name": "통합계좌", "min_balance": 5000000, "max_balance": 200000000},
    1: {"name": "수시입출금", "min_balance": 100000, "max_balance": 5000000},
    2: {"name": "예적금", "min_balance": 1000000, "max_balance": 100000000},
    6: {"name": "수익증권", "min_balance": 500000, "max_balance": 50000000}
}

# ============================================================================
# 통계 변수
# ============================================================================

stats = {
    "users_created": 0,
    "users_failed": 0,
    "customers_created": 0,
    "customers_failed": 0,
    "accounts_created": 0,
    "accounts_failed": 0,
    "datacode_success": False
}

# ============================================================================
# User 서버 함수
# ============================================================================

def create_user(user_data):
    """User 서버에 사용자 생성 및 CI 발급"""
    url = f"{USER_SERVER}/api/user/create"
    
    payload = {
        "name": user_data["name"],
        "birthDate": user_data["birthDate"],
        "gender": user_data["gender"],
        "residentNumber": user_data["residentNumber"],
        "phone": user_data["phone"]
    }
    
    try:
        response = requests.post(url, json=payload, headers={"Content-Type": "application/json"}, timeout=10)
        
        if response.status_code in [200, 201]:
            result = response.json()
            ci = result.get('ci')
            print(f"  ✅ User 생성 성공: {user_data['name']} (CI: {ci[:20]}...)")
            stats["users_created"] += 1
            return ci
        else:
            print(f"  ❌ User 생성 실패: {user_data['name']} - 상태코드: {response.status_code}")
            print(f"     응답: {response.text}")
            stats["users_failed"] += 1
            return None
            
    except requests.exceptions.RequestException as e:
        print(f"  ❌ User 생성 오류: {user_data['name']} - {str(e)}")
        stats["users_failed"] += 1
        return None

# ============================================================================
# 은행 서버 함수
# ============================================================================

def create_bank_customer(bank_key, bank_config, user_data, ci):
    """은행에 고객 등록"""
    url = f"{bank_config['url']}{bank_config['api_prefix']}/customers"
    
    payload = {
        "ci": ci,
        "name": user_data["name"],
        "gender": user_data["gender"],
        "birthDate": user_data["birthDate"],
        "phone": user_data["phone"]
    }
    
    try:
        response = requests.post(url, json=payload, headers={"Content-Type": "application/json"}, timeout=10)
        
        if response.status_code in [200, 201]:
            print(f"    ✅ {bank_config['name']} 고객 등록 성공")
            stats["customers_created"] += 1
            return True
        else:
            print(f"    ❌ {bank_config['name']} 고객 등록 실패 - 상태코드: {response.status_code}")
            stats["customers_failed"] += 1
            return False
            
    except requests.exceptions.RequestException as e:
        print(f"    ❌ {bank_config['name']} 고객 등록 오류 - {str(e)}")
        stats["customers_failed"] += 1
        return False

def create_bank_account(bank_key, bank_config, ci, account_type):
    """은행에 계좌 생성"""
    url = f"{bank_config['url']}{bank_config['api_prefix']}/accounts"
    
    # 계좌 타입별 랜덤 잔액 생성
    account_info = ACCOUNT_TYPES[account_type]
    balance = random.randint(account_info["min_balance"], account_info["max_balance"])
    
    # 계좌 개설일 (최근 1년 내 랜덤)
    days_ago = random.randint(30, 365)
    opening_date = (datetime.now() - timedelta(days=days_ago)).strftime("%Y-%m-%d")
    
    payload = {
        "customerCi": ci,
        "accountType": account_type,
        "balance": balance,
        "openingDate": opening_date
    }
    
    try:
        response = requests.post(url, json=payload, headers={"Content-Type": "application/json"}, timeout=10)
        
        if response.status_code in [200, 201]:
            result = response.json()
            account_number = result.get('accountNumber', 'N/A')
            print(f"      ✅ {account_info['name']} 계좌 생성: {account_number} (잔액: {balance:,}원)")
            stats["accounts_created"] += 1
            return True
        else:
            print(f"      ❌ {account_info['name']} 계좌 생성 실패 - 상태코드: {response.status_code}")
            stats["accounts_failed"] += 1
            return False
            
    except requests.exceptions.RequestException as e:
        print(f"      ❌ {account_info['name']} 계좌 생성 오류 - {str(e)}")
        stats["accounts_failed"] += 1
        return False

# ============================================================================
# hanainplan 서버 데이터코드 삽입
# ============================================================================

def insert_hanainplan_datacodes():
    """hanainplan 서버에 업종코드/질병코드 데이터 삽입"""
    print("\n" + "=" * 80)
    print("📋 hanainplan 서버 데이터코드 삽입 중...")
    print("=" * 80)
    
    try:
        import pymysql
    except ImportError:
        print("❌ pymysql 라이브러리가 필요합니다. 설치: pip3 install pymysql")
        return False
    
    sql_file_path = "/Users/jusoyeong/Desktop/HANAinPLAN_OpenBanking/backend/hanainplan/datacode.sql"
    
    try:
        # SQL 파일 읽기
        with open(sql_file_path, 'r', encoding='utf-8') as f:
            sql_content = f.read()
        
        print(f"✅ SQL 파일 읽기 성공: {sql_file_path}")
        
        # MySQL 연결 (Docker 컨테이너 외부에서 접근)
        connection = pymysql.connect(
            host='localhost',
            port=3306,
            user='hanainplan_user',
            password='hanainplan_pass123',
            database='hanainplan_db',
            charset='utf8mb4'
        )
        
        print("✅ MySQL 연결 성공")
        
        cursor = connection.cursor()
        
        # SQL 문 실행 (세미콜론으로 분리)
        sql_statements = sql_content.split(';')
        
        for i, statement in enumerate(sql_statements):
            statement = statement.strip()
            if statement:
                try:
                    cursor.execute(statement)
                    print(f"  ✅ SQL 문 {i+1} 실행 성공")
                except Exception as e:
                    print(f"  ⚠️  SQL 문 {i+1} 실행 경고: {str(e)}")
        
        connection.commit()
        
        # 삽입 결과 확인
        cursor.execute("SELECT COUNT(*) FROM industry_code")
        industry_count = cursor.fetchone()[0]
        
        cursor.execute("SELECT COUNT(*) FROM disease_code")
        disease_count = cursor.fetchone()[0]
        
        print(f"\n✅ 데이터 삽입 완료:")
        print(f"  - 업종코드: {industry_count}개")
        print(f"  - 질병코드: {disease_count}개")
        
        cursor.close()
        connection.close()
        
        stats["datacode_success"] = True
        return True
        
    except FileNotFoundError:
        print(f"❌ SQL 파일을 찾을 수 없습니다: {sql_file_path}")
        return False
    except Exception as e:
        print(f"❌ 데이터코드 삽입 오류: {str(e)}")
        return False

# ============================================================================
# 랜덤 로직
# ============================================================================

def select_random_banks():
    """1-3개 은행을 랜덤 선택"""
    num_banks = random.randint(1, 3)
    bank_keys = list(BANK_CONFIGS.keys())
    selected = random.sample(bank_keys, num_banks)
    return selected

def generate_random_accounts():
    """각 은행당 1-3개 계좌 타입 선택"""
    num_accounts = random.randint(1, 3)
    account_types = list(ACCOUNT_TYPES.keys())
    selected = random.sample(account_types, min(num_accounts, len(account_types)))
    return selected

# ============================================================================
# 메인 실행
# ============================================================================

def main():
    """메인 실행 함수"""
    print("\n" + "=" * 80)
    print("🚀 HANAinPLAN 통합 테스트 데이터 삽입 시작")
    print("=" * 80)
    print(f"📊 생성할 사용자: {len(TEST_USERS)}명")
    print(f"🏦 은행 서버: {', '.join([config['name'] for config in BANK_CONFIGS.values()])}")
    print("=" * 80)
    
    # 1. hanainplan 서버 데이터코드 삽입
    insert_hanainplan_datacodes()
    
    time.sleep(2)
    
    # 2. 사용자별 데이터 생성
    print("\n" + "=" * 80)
    print("👥 사용자 및 계좌 데이터 생성 중...")
    print("=" * 80)
    
    for i, user_data in enumerate(TEST_USERS, 1):
        print(f"\n[{i}/{len(TEST_USERS)}] 사용자: {user_data['name']}")
        print("-" * 80)
        
        # User 서버에 등록
        ci = create_user(user_data)
        
        if not ci:
            print(f"  ⚠️  {user_data['name']} 사용자 생성 실패 - 은행 계좌 생성 스킵")
            continue
        
        time.sleep(0.5)
        
        # 랜덤으로 1-3개 은행 선택
        selected_banks = select_random_banks()
        print(f"  🎲 선택된 은행: {', '.join([BANK_CONFIGS[bank]['name'] for bank in selected_banks])}")
        
        # 각 은행에 고객 등록 및 계좌 생성
        for bank_key in selected_banks:
            bank_config = BANK_CONFIGS[bank_key]
            
            # 고객 등록
            customer_created = create_bank_customer(bank_key, bank_config, user_data, ci)
            
            if not customer_created:
                continue
            
            time.sleep(0.3)
            
            # 1-3개 계좌 생성
            account_types = generate_random_accounts()
            
            for account_type in account_types:
                create_bank_account(bank_key, bank_config, ci, account_type)
                time.sleep(0.3)
        
        # 사용자 간 간격
        if i < len(TEST_USERS):
            time.sleep(1)
    
    # 3. 최종 통계 출력
    print("\n" + "=" * 80)
    print("📊 데이터 삽입 완료 통계")
    print("=" * 80)
    print(f"👥 사용자:")
    print(f"  - 성공: {stats['users_created']}명")
    print(f"  - 실패: {stats['users_failed']}명")
    print(f"\n🏦 은행 고객:")
    print(f"  - 성공: {stats['customers_created']}개")
    print(f"  - 실패: {stats['customers_failed']}개")
    print(f"\n💳 계좌:")
    print(f"  - 성공: {stats['accounts_created']}개")
    print(f"  - 실패: {stats['accounts_failed']}개")
    print(f"\n📋 hanainplan 데이터코드:")
    print(f"  - {'성공 ✅' if stats['datacode_success'] else '실패 ❌'}")
    
    print("\n" + "=" * 80)
    
    if stats['users_created'] > 0 or stats['accounts_created'] > 0:
        print("✅ 데이터 삽입이 성공적으로 완료되었습니다!")
        print("\n🔍 데이터 확인 방법:")
        print(f"  User 서버: curl {USER_SERVER}/api/user/all")
        print(f"  하나은행: curl {BANK_CONFIGS['hana']['url']}{BANK_CONFIGS['hana']['api_prefix']}/customers")
        print(f"  신한은행: curl {BANK_CONFIGS['shinhan']['url']}{BANK_CONFIGS['shinhan']['api_prefix']}/customers")
        print(f"  국민은행: curl {BANK_CONFIGS['kookmin']['url']}{BANK_CONFIGS['kookmin']['api_prefix']}/customers")
        print(f"  hanainplan: curl {HANAINPLAN_SERVER}/api/industries")
    else:
        print("⚠️  일부 데이터 삽입에 실패했습니다. 위의 오류 메시지를 확인하세요.")
    
    print("=" * 80 + "\n")

if __name__ == "__main__":
    main()

