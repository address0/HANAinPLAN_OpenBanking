#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
HANAinPLAN 오픈뱅킹 프로젝트 통합 데이터 삽입 스크립트
- 하나은행, 신한은행, 국민은행 샘플 데이터
- HANAinPLAN 상품 코드 데이터
- 보험사 샘플 데이터
"""

import mysql.connector
from mysql.connector import Error
import os
from datetime import datetime, date, timedelta
import random

# 데이터베이스 연결 정보
DB_CONFIGS = {
    'hanainplan': {
        'host': os.getenv('HANAINPLAN_DB_HOST', 'localhost'),
        'port': int(os.getenv('HANAINPLAN_DB_PORT', '3306')),
        'database': 'hanainplan_db',
        'user': os.getenv('MYSQL_USER', 'hanainplan_user'),
        'password': os.getenv('MYSQL_PASSWORD', 'hanainplan_pass123')
    },
    'hana': {
        'host': os.getenv('HANA_DB_HOST', 'localhost'),
        'port': int(os.getenv('HANA_DB_PORT', '3307')),
        'database': 'hana_bank',
        'user': os.getenv('MYSQL_USER', 'root'),
        'password': os.getenv('MYSQL_PASSWORD', 'root1234')
    },
    'shinhan': {
        'host': os.getenv('SHINHAN_DB_HOST', 'localhost'),
        'port': int(os.getenv('SHINHAN_DB_PORT', '3308')),
        'database': 'shinhan_bank',
        'user': os.getenv('MYSQL_USER', 'root'),
        'password': os.getenv('MYSQL_PASSWORD', 'root1234')
    },
    'kookmin': {
        'host': os.getenv('KOOKMIN_DB_HOST', 'localhost'),
        'port': int(os.getenv('KOOKMIN_DB_PORT', '3309')),
        'database': 'kookmin_bank',
        'user': os.getenv('MYSQL_USER', 'root'),
        'password': os.getenv('MYSQL_PASSWORD', 'root1234')
    }
}

# 상품 코드 데이터
DEPOSIT_PRODUCTS = [
    {
        'deposit_code': 'HANA-DEP-001',
        'name': '하나 정기예금',
        'bank_code': 'HANA',
        'bank_name': '하나은행',
        'description': '하나은행 정기예금 상품입니다. 안정적인 이자 수익을 제공합니다.'
    },
    {
        'deposit_code': 'SHINHAN-DEP-001',
        'name': '신한 정기예금',
        'bank_code': 'SHINHAN',
        'bank_name': '신한은행',
        'description': '신한은행 정기예금 상품입니다. 우대금리 혜택이 있습니다.'
    },
    {
        'deposit_code': 'KOOKMIN-DEP-001',
        'name': '국민 정기예금',
        'bank_code': 'KOOKMIN',
        'bank_name': '국민은행',
        'description': '국민은행 정기예금 상품입니다. 다양한 만기 선택이 가능합니다.'
    }
]

INSURANCE_PRODUCTS = [
    {
        'insurance_code': 'HANA-INS-001',
        'name': '하나생명 이율보증형보험',
        'insurer_code': 'HANA',
        'insurer_name': '하나생명',
        'description': '하나생명 이율보증형 보험 상품입니다. 확정금리로 안정적인 수익을 보장합니다.'
    },
    {
        'insurance_code': 'HANHWA-INS-001',
        'name': '한화생명 이율보증형보험',
        'insurer_code': 'HANHWA',
        'insurer_name': '한화생명',
        'description': '한화생명 이율보증형 보험 상품입니다. 세제혜택과 함께 안정적인 수익을 제공합니다.'
    },
    {
        'insurance_code': 'SAMSUNG-INS-001',
        'name': '삼성생명 이율보증형보험',
        'insurer_code': 'SAMSUNG',
        'insurer_name': '삼성생명',
        'description': '삼성생명 이율보증형 보험 상품입니다. 높은 이율과 안정성을 동시에 제공합니다.'
    }
]


def print_header(title):
    """섹션 헤더 출력"""
    print("\n" + "="*70)
    print(f"  {title}")
    print("="*70)


def print_section(title):
    """서브섹션 헤더 출력"""
    print(f"\n📋 {title}")
    print("-" * 70)


def create_connection(db_name):
    """MySQL 데이터베이스 연결 생성"""
    config = DB_CONFIGS.get(db_name)
    if not config:
        print(f"❌ 잘못된 DB 이름: {db_name}")
        return None
    
    try:
        connection = mysql.connector.connect(**config)
        if connection.is_connected():
            print(f"✅ {db_name.upper()} DB 연결 성공: {config['host']}:{config['port']}/{config['database']}")
            return connection
    except Error as e:
        print(f"❌ {db_name.upper()} DB 연결 실패: {e}")
        return None


# ============================================================================
# HANAinPLAN 데이터 삽입
# ============================================================================

def insert_hanainplan_product_codes(connection):
    """HANAinPLAN 상품 코드 삽입"""
    cursor = connection.cursor()
    
    # 예금 상품 코드
    deposit_query = """
    INSERT INTO tb_deposit_product 
    (deposit_code, name, bank_code, bank_name, description, created_at, updated_at)
    VALUES (%s, %s, %s, %s, %s, NOW(), NOW())
    ON DUPLICATE KEY UPDATE 
        name = VALUES(name),
        bank_name = VALUES(bank_name),
        description = VALUES(description),
        updated_at = NOW()
    """
    
    print("\n  📊 예금 상품 코드 삽입...")
    for product in DEPOSIT_PRODUCTS:
        try:
            cursor.execute(deposit_query, (
                product['deposit_code'],
                product['name'],
                product['bank_code'],
                product['bank_name'],
                product['description']
            ))
            print(f"    ✓ {product['name']} ({product['deposit_code']})")
        except Error as e:
            print(f"    ✗ {product['name']} 실패: {e}")
    
    # 이율보증형 보험 상품 코드
    insurance_query = """
    INSERT INTO tb_insurance_product 
    (insurance_code, name, insurer_code, insurer_name, description, created_at, updated_at)
    VALUES (%s, %s, %s, %s, %s, NOW(), NOW())
    ON DUPLICATE KEY UPDATE 
        name = VALUES(name),
        insurer_name = VALUES(insurer_name),
        description = VALUES(description),
        updated_at = NOW()
    """
    
    print("\n  🏥 이율보증형 보험 상품 코드 삽입...")
    for product in INSURANCE_PRODUCTS:
        try:
            cursor.execute(insurance_query, (
                product['insurance_code'],
                product['name'],
                product['insurer_code'],
                product['insurer_name'],
                product['description']
            ))
            print(f"    ✓ {product['name']} ({product['insurance_code']})")
        except Error as e:
            print(f"    ✗ {product['name']} 실패: {e}")
    
    connection.commit()
    cursor.close()
    print(f"\n  ✅ HANAinPLAN 상품 코드 삽입 완료")


# ============================================================================
# 하나은행 데이터 삽입
# ============================================================================

def insert_hana_bank_data(connection):
    """하나은행 샘플 데이터 삽입"""
    cursor = connection.cursor()
    
    # 고객 데이터
    print("\n  👥 고객 데이터 삽입...")
    customers = [
        ('CI1234567890ABCDEFGHIJK1234567890ABCDEFGHIJK1234567890ABC1', '홍길동', 'M', '19900101', '010-1234-5678'),
        ('CI9876543210ZYXWVUTSRQPO9876543210ZYXWVUTSRQPO9876543210ZYX1', '김영희', 'F', '19920515', '010-9876-5432'),
        ('CI5555666677778888AAAA5555666677778888AAAA5555666677778888', '이철수', 'M', '19880320', '010-5555-6666'),
    ]
    
    customer_query = """
    INSERT INTO hana_customers (ci, name, gender, birth_date, phone, created_at)
    VALUES (%s, %s, %s, %s, %s, NOW())
    ON DUPLICATE KEY UPDATE name = VALUES(name), phone = VALUES(phone)
    """
    
    for customer in customers:
        try:
            cursor.execute(customer_query, customer)
            print(f"    ✓ {customer[1]} ({customer[2]}, {customer[3][:4]}년생)")
        except Error as e:
            print(f"    ✗ {customer[1]} 실패: {e}")
    
    # 계좌 데이터
    print("\n  🏦 계좌 데이터 삽입...")
    accounts = [
        ('110-1234-567890', 1, 5000000.00, date(2024, 1, 15), customers[0][0]),
        ('110-2345-678901', 2, 10000000.00, date(2024, 2, 10), customers[0][0]),
        ('110-3456-789012', 1, 3000000.00, date(2024, 3, 5), customers[1][0]),
        ('110-4567-890123', 1, 8000000.00, date(2023, 12, 1), customers[2][0]),
    ]
    
    account_query = """
    INSERT INTO hana_accounts (account_number, account_type, balance, opening_date, customer_ci, created_at, updated_at)
    VALUES (%s, %s, %s, %s, %s, NOW(), NOW())
    ON DUPLICATE KEY UPDATE balance = VALUES(balance), updated_at = NOW()
    """
    
    for account in accounts:
        try:
            cursor.execute(account_query, account)
            print(f"    ✓ {account[0]} (잔액: {account[2]:,.0f}원)")
        except Error as e:
            print(f"    ✗ {account[0]} 실패: {e}")
    
    # 예금 가입 정보
    print("\n  💰 예금 가입 정보 삽입...")
    today = date.today()
    subscriptions = [
        (customers[0][0], accounts[1][0], 'ACTIVE', today - timedelta(days=30), today + timedelta(days=335), 
         12, 0, 'HANA-DEP-001', 0.0350, 10000000.00, 0.00, today, today + timedelta(days=30)),
        (customers[1][0], accounts[2][0], 'ACTIVE', today - timedelta(days=60), today + timedelta(days=305),
         12, 0, 'HANA-DEP-001', 0.0340, 3000000.00, 0.00, today, today + timedelta(days=30)),
    ]
    
    subscription_query = """
    INSERT INTO hana_deposit_subscriptions 
    (customer_ci, account_number, status, subscription_date, maturity_date, contract_period, 
     product_type, deposit_code, rate, current_balance, unpaid_interest, 
     last_interest_calculation_date, next_interest_payment_date, created_at, updated_at)
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, NOW(), NOW())
    ON DUPLICATE KEY UPDATE 
        current_balance = VALUES(current_balance),
        unpaid_interest = VALUES(unpaid_interest),
        updated_at = NOW()
    """
    
    for sub in subscriptions:
        try:
            cursor.execute(subscription_query, sub)
            print(f"    ✓ {sub[1]} (가입일: {sub[3]}, 금리: {sub[8]*100:.2f}%)")
        except Error as e:
            print(f"    ✗ {sub[1]} 실패: {e}")
    
    connection.commit()
    cursor.close()
    print(f"\n  ✅ 하나은행 데이터 삽입 완료")


# ============================================================================
# 신한은행 데이터 삽입
# ============================================================================

def insert_shinhan_bank_data(connection):
    """신한은행 샘플 데이터 삽입 (하나은행과 유사한 구조)"""
    cursor = connection.cursor()
    
    print("\n  👥 고객 데이터 삽입...")
    customers = [
        ('CI1234567890ABCDEFGHIJK1234567890ABCDEFGHIJK1234567890ABC1', '홍길동', 'M', '19900101', '010-1234-5678'),
        ('CI9876543210ZYXWVUTSRQPO9876543210ZYXWVUTSRQPO9876543210ZYX1', '김영희', 'F', '19920515', '010-9876-5432'),
    ]
    
    customer_query = """
    INSERT INTO shinhan_customers (ci, name, gender, birth_date, phone, created_at)
    VALUES (%s, %s, %s, %s, %s, NOW())
    ON DUPLICATE KEY UPDATE name = VALUES(name), phone = VALUES(phone)
    """
    
    for customer in customers:
        cursor.execute(customer_query, customer)
        print(f"    ✓ {customer[1]}")
    
    print("\n  🏦 계좌 데이터 삽입...")
    accounts = [
        ('140-5678-901234', 1, 4000000.00, date(2024, 1, 20), customers[0][0]),
        ('140-6789-012345', 1, 6000000.00, date(2024, 2, 15), customers[1][0]),
    ]
    
    account_query = """
    INSERT INTO shinhan_accounts (account_number, account_type, balance, opening_date, customer_ci, created_at, updated_at)
    VALUES (%s, %s, %s, %s, %s, NOW(), NOW())
    ON DUPLICATE KEY UPDATE balance = VALUES(balance), updated_at = NOW()
    """
    
    for account in accounts:
        cursor.execute(account_query, account)
        print(f"    ✓ {account[0]} (잔액: {account[2]:,.0f}원)")
    
    connection.commit()
    cursor.close()
    print(f"\n  ✅ 신한은행 데이터 삽입 완료")


# ============================================================================
# 국민은행 데이터 삽입
# ============================================================================

def insert_kookmin_bank_data(connection):
    """국민은행 샘플 데이터 삽입 (하나은행과 유사한 구조)"""
    cursor = connection.cursor()
    
    print("\n  👥 고객 데이터 삽입...")
    customers = [
        ('CI1234567890ABCDEFGHIJK1234567890ABCDEFGHIJK1234567890ABC1', '홍길동', 'M', '19900101', '010-1234-5678'),
        ('CI5555666677778888AAAA5555666677778888AAAA5555666677778888', '이철수', 'M', '19880320', '010-5555-6666'),
    ]
    
    customer_query = """
    INSERT INTO kookmin_customers (ci, name, gender, birth_date, phone, created_at)
    VALUES (%s, %s, %s, %s, %s, NOW())
    ON DUPLICATE KEY UPDATE name = VALUES(name), phone = VALUES(phone)
    """
    
    for customer in customers:
        cursor.execute(customer_query, customer)
        print(f"    ✓ {customer[1]}")
    
    print("\n  🏦 계좌 데이터 삽입...")
    accounts = [
        ('004-7890-123456', 1, 7000000.00, date(2024, 1, 10), customers[0][0]),
        ('004-8901-234567', 1, 9000000.00, date(2023, 11, 25), customers[1][0]),
    ]
    
    account_query = """
    INSERT INTO kookmin_accounts (account_number, account_type, balance, opening_date, customer_ci, created_at, updated_at)
    VALUES (%s, %s, %s, %s, %s, NOW(), NOW())
    ON DUPLICATE KEY UPDATE balance = VALUES(balance), updated_at = NOW()
    """
    
    for account in accounts:
        cursor.execute(account_query, account)
        print(f"    ✓ {account[0]} (잔액: {account[2]:,.0f}원)")
    
    connection.commit()
    cursor.close()
    print(f"\n  ✅ 국민은행 데이터 삽입 완료")


# ============================================================================
# 메인 실행
# ============================================================================

def main():
    """메인 실행 함수"""
    print_header("HANAinPLAN 오픈뱅킹 통합 데이터 삽입 스크립트")
    print(f"실행 시간: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
    
    results = {
        'success': [],
        'failed': []
    }
    
    # 1. HANAinPLAN 데이터 삽입
    print_section("1. HANAinPLAN 백엔드 - 상품 코드 삽입")
    conn = create_connection('hanainplan')
    if conn:
        try:
            insert_hanainplan_product_codes(conn)
            results['success'].append('HANAinPLAN')
        except Exception as e:
            print(f"❌ HANAinPLAN 데이터 삽입 실패: {e}")
            results['failed'].append('HANAinPLAN')
        finally:
            conn.close()
    else:
        results['failed'].append('HANAinPLAN')
    
    # 2. 하나은행 데이터 삽입
    print_section("2. 하나은행 백엔드 - 샘플 데이터 삽입")
    conn = create_connection('hana')
    if conn:
        try:
            insert_hana_bank_data(conn)
            results['success'].append('하나은행')
        except Exception as e:
            print(f"❌ 하나은행 데이터 삽입 실패: {e}")
            results['failed'].append('하나은행')
        finally:
            conn.close()
    else:
        results['failed'].append('하나은행')
    
    # 3. 신한은행 데이터 삽입
    print_section("3. 신한은행 백엔드 - 샘플 데이터 삽입")
    conn = create_connection('shinhan')
    if conn:
        try:
            insert_shinhan_bank_data(conn)
            results['success'].append('신한은행')
        except Exception as e:
            print(f"❌ 신한은행 데이터 삽입 실패: {e}")
            results['failed'].append('신한은행')
        finally:
            conn.close()
    else:
        results['failed'].append('신한은행')
    
    # 4. 국민은행 데이터 삽입
    print_section("4. 국민은행 백엔드 - 샘플 데이터 삽입")
    conn = create_connection('kookmin')
    if conn:
        try:
            insert_kookmin_bank_data(conn)
            results['success'].append('국민은행')
        except Exception as e:
            print(f"❌ 국민은행 데이터 삽입 실패: {e}")
            results['failed'].append('국민은행')
        finally:
            conn.close()
    else:
        results['failed'].append('국민은행')
    
    # 결과 요약
    print_header("📊 데이터 삽입 결과 요약")
    print(f"\n✅ 성공: {len(results['success'])}개")
    for name in results['success']:
        print(f"  ✓ {name}")
    
    if results['failed']:
        print(f"\n❌ 실패: {len(results['failed'])}개")
        for name in results['failed']:
            print(f"  ✗ {name}")
    
    print("\n" + "="*70)
    if not results['failed']:
        print("🎉 모든 데이터 삽입이 성공적으로 완료되었습니다!")
    else:
        print("⚠️  일부 데이터 삽입에 실패했습니다. 위 로그를 확인하세요.")
    print("="*70 + "\n")


if __name__ == "__main__":
    main()





