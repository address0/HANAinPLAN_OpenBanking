#!/usr/bin/env python3
"""
HANA 금융상품 테이블에 샘플 데이터를 삽입하는 스크립트
"""

import requests
import json
import time
from datetime import datetime

# API 엔드포인트 설정
PRODUCTS_URL = "http://localhost:8080/api/hana/products"
ACCOUNTS_URL = "http://localhost:8080/api/hana/accounts"

# 샘플 금융상품 데이터
sample_products = [
    {
        "productCode": "1479088",
        "productName": "하나의 정기예금",
        "depositType": "정기예금",
        "minContractPeriod": 1,
        "maxContractPeriod": 60,
        "contractPeriodUnit": "개월",
        "subscriptionTarget": "실명의 개인 또는 개인사업자",
        "subscriptionAmount": 1000000,
        "productCategory": "예금",
        "interestPayment": "만기일시지급식",
        "taxBenefit": "비과세종합저축으로 가입 가능 (전 금융기관 통합한도 범위 내) 단, 만기 후 이자는 소득세 부과",
        "partialWithdrawal": "만기일 이전 2회까지 가능하며, 일부해지 금액에 대해서는 중도해지금리 적용",
        "cancellationPenalty": "",
        "description": "자유롭게 자금관리가 가능한 하나원큐(스마트폰 뱅킹) 전용 정기예금"
    },
    {
        "productCode": "1419635",
        "productName": "1년 연동형 정기예금",
        "depositType": "정기예금",
        "minContractPeriod": 12,
        "maxContractPeriod": 180,
        "contractPeriodUnit": "개월",
        "subscriptionTarget": "개인, 개인사업자 또는 법인",
        "subscriptionAmount": 10000,
        "productCategory": "예금",
        "interestPayment": "만기일시지급식",
        "taxBenefit": "개인의 경우 비과세종합저축 가능(전 금융기관 통합한도 범위내), 만기 후 이자는 소득세 부과",
        "partialWithdrawal": "불가",
        "cancellationPenalty": "",
        "description": "서울보증보험의 보증서 발급 담보용 정기예금"
    },
    {
        "productCode": "1419664",
        "productName": "행복knowhow 연금예금",
        "depositType": "정기예금",
        "minContractPeriod": 12,
        "maxContractPeriod": 360,
        "contractPeriodUnit": "개월",
        "subscriptionTarget": "실명의 개인 또는 개인사업자",
        "subscriptionAmount": 1000000,
        "productCategory": "적금",
        "interestPayment": "원리금균등지급식",
        "taxBenefit": "비과세종합저축으로 가입 가능 (전 금융기관 통합한도 범위 내) 단, 만기 후 이자는 소득세 부과",
        "partialWithdrawal": "거치기간 동안에만 총3회까지 가능 (최소가입금액 1백만원 이상유지, 분할해지시 중도해지금리 적용)",
        "cancellationPenalty": "",
        "description": "노후자금, 생활자금, 재투자자금까지! 행복knowhow 연금예금으로 설계하세요!"
    },
    {
        "productCode": "1419602",
        "productName": "정기예금",
        "depositType": "정기예금",
        "minContractPeriod": 1,
        "maxContractPeriod": 36,
        "contractPeriodUnit": "개월",
        "subscriptionTarget": "제한없음",
        "subscriptionAmount": 10000,
        "productCategory": "예금",
        "interestPayment": "만기일시지급식",
        "taxBenefit": "비과세종합저축으로 가입 가능 (전 금융기관 통합한도 범위내)",
        "partialWithdrawal": "만기해지 포함 총3회 일부해지 가능 (일부해지 후 잔액은 최소가입금액 이상이어야 함)",
        "cancellationPenalty": "",
        "description": "목돈을 일정기간 동안 예치하여 안정적인 수익을 추구하는 예금"
    },
    {
        "productCode": "1419600",
        "productName": "고단위 플러스",
        "depositType": "정기예금",
        "minContractPeriod": 1,
        "maxContractPeriod": 60,
        "contractPeriodUnit": "개월",
        "subscriptionTarget": "주택청약자",
        "subscriptionAmount": 50000,
        "productCategory": "저축",
        "interestPayment": "만기일시지급",
        "taxBenefit": "비과세",
        "partialWithdrawal": "가능",
        "cancellationPenalty": "",
        "description": "주택 구매를 위한 종합저축 상품으로, 주택청약자에게 특별한 혜택을 제공합니다. 월 정기 납입으로 꾸준한 저축 습관을 기를 수 있습니다."
    },
    {
        "productCode": "HANA006",
        "productName": "하나 연금저축",
        "depositType": "연금저축",
        "minContractPeriod": 60,
        "maxContractPeriod": 240,
        "contractPeriodUnit": "개월",
        "subscriptionTarget": "개인",
        "subscriptionAmount": 700000,
        "productCategory": "연금",
        "interestPayment": "만기일시지급",
        "taxBenefit": "비과세",
        "partialWithdrawal": "가능",
        "cancellationPenalty": "중도해지 시 이자율 감소",
        "description": "노후 생활을 위한 연금저축 상품으로, 세제 혜택을 받으면서 장기적으로 자산을 늘릴 수 있습니다. 매월 일정 금액을 납입하여 안정적인 노후 준비를 할 수 있습니다."
    },
    {
        "productCode": "HANA007",
        "productName": "하나 우대정기예금",
        "depositType": "정기예금",
        "minContractPeriod": 12,
        "maxContractPeriod": 24,
        "contractPeriodUnit": "개월",
        "subscriptionTarget": "개인",
        "subscriptionAmount": 5000000,
        "productCategory": "예금",
        "interestPayment": "만기일시지급",
        "taxBenefit": "비과세",
        "partialWithdrawal": "불가",
        "cancellationPenalty": "중도해지 시 이자율 감소",
        "description": "대액 예금자에게 특별한 우대금리를 제공하는 정기예금 상품입니다. 높은 금액의 예금으로 더 많은 수익을 얻을 수 있는 프리미엄 상품입니다."
    },
    {
        "productCode": "HANA008",
        "productName": "하나 마이너스통장",
        "depositType": "마이너스통장",
        "minContractPeriod": 12,
        "maxContractPeriod": 60,
        "contractPeriodUnit": "개월",
        "subscriptionTarget": "개인",
        "subscriptionAmount": 10000000,
        "productCategory": "대출",
        "interestPayment": "이자 납입",
        "taxBenefit": "과세",
        "partialWithdrawal": "가능",
        "cancellationPenalty": "중도해지 시 수수료 발생",
        "description": "예금과 대출이 결합된 혁신적인 금융상품으로, 필요할 때 언제든지 자금을 활용할 수 있습니다. 일상적인 자금 관리와 긴급 자금 조달에 유용합니다."
    },
    {
        "productCode": "HANA009",
        "productName": "하나 전세자금대출",
        "depositType": "전세자금대출",
        "minContractPeriod": 24,
        "maxContractPeriod": 120,
        "contractPeriodUnit": "개월",
        "subscriptionTarget": "전세자금 필요자",
        "subscriptionAmount": 300000000,
        "productCategory": "대출",
        "interestPayment": "원리금균등분할상환",
        "taxBenefit": "과세",
        "partialWithdrawal": "가능",
        "cancellationPenalty": "중도상환 시 수수료 발생",
        "description": "전세 보증금 마련을 위한 대출 상품으로, 합리적인 금리와 유연한 상환 조건을 제공합니다. 주거 안정을 위한 필수 금융상품입니다."
    },
    {
        "productCode": "HANA010",
        "productName": "하나 신용대출",
        "depositType": "신용대출",
        "minContractPeriod": 12,
        "maxContractPeriod": 84,
        "contractPeriodUnit": "개월",
        "subscriptionTarget": "개인",
        "subscriptionAmount": 50000000,
        "productCategory": "대출",
        "interestPayment": "원리금균등분할상환",
        "taxBenefit": "과세",
        "partialWithdrawal": "가능",
        "cancellationPenalty": "중도상환 시 수수료 발생",
        "description": "담보 없이 신용만으로 받을 수 있는 대출 상품으로, 다양한 용도로 활용 가능합니다. 신용도에 따라 차등 금리가 적용되어 합리적인 대출 조건을 제공합니다."
    }
]

# 샘플 계좌 데이터
sample_accounts = [
    {
        "accountNumber": "1001234567890",
        "accountType": 1,  # 수시입출금
        "balance": 500000,
        "openingDate": "2024-01-15",
        "thirdPartyConsent": True,
        "withdrawalConsent": True,
        "userId": 1
    },
    {
        "accountNumber": "1001234567891",
        "accountType": 2,  # 예적금
        "balance": 1000000,
        "openingDate": "2024-02-01",
        "maturityDate": "2025-02-01",
        "thirdPartyConsent": True,
        "withdrawalConsent": False,
        "userId": 1
    },
    {
        "accountNumber": "1001234567892",
        "accountType": 1,  # 수시입출금
        "balance": 2500000,
        "openingDate": "2024-01-20",
        "thirdPartyConsent": True,
        "withdrawalConsent": True,
        "userId": 2
    },
    {
        "accountNumber": "1001234567893",
        "accountType": 2,  # 예적금
        "balance": 5000000,
        "openingDate": "2024-03-01",
        "maturityDate": "2026-03-01",
        "thirdPartyConsent": True,
        "withdrawalConsent": False,
        "userId": 2
    },
    {
        "accountNumber": "1001234567894",
        "accountType": 1,  # 수시입출금
        "balance": 750000,
        "openingDate": "2024-02-15",
        "thirdPartyConsent": True,
        "withdrawalConsent": True,
        "userId": 3
    },
    {
        "accountNumber": "1001234567895",
        "accountType": 6,  # 수익증권
        "balance": 2000000,
        "openingDate": "2024-01-10",
        "thirdPartyConsent": True,
        "withdrawalConsent": True,
        "userId": 3
    },
    {
        "accountNumber": "1001234567896",
        "accountType": 1,  # 수시입출금
        "balance": 1200000,
        "openingDate": "2024-03-01",
        "thirdPartyConsent": True,
        "withdrawalConsent": True,
        "userId": 4
    },
    {
        "accountNumber": "1001234567897",
        "accountType": 2,  # 예적금
        "balance": 3000000,
        "openingDate": "2024-02-20",
        "maturityDate": "2025-08-20",
        "thirdPartyConsent": True,
        "withdrawalConsent": False,
        "userId": 4
    },
    {
        "accountNumber": "1001234567898",
        "accountType": 1,  # 수시입출금
        "balance": 1800000,
        "openingDate": "2024-01-25",
        "thirdPartyConsent": True,
        "withdrawalConsent": True,
        "userId": 5
    },
    {
        "accountNumber": "1001234567899",
        "accountType": 0,  # 통합계좌
        "balance": 8000000,
        "openingDate": "2024-01-01",
        "thirdPartyConsent": True,
        "withdrawalConsent": True,
        "userId": 5
    }
]

def insert_product(product_data):
    """단일 상품 데이터를 API에 삽입"""
    try:
        response = requests.post(
            PRODUCTS_URL,
            json=product_data,
            headers={'Content-Type': 'application/json'},
            timeout=10
        )
        
        if response.status_code == 201:
            print(f"✅ 성공: {product_data['productName']} ({product_data['productCode']})")
            return True
        else:
            print(f"❌ 실패: {product_data['productName']} - 상태코드: {response.status_code}")
            print(f"   응답: {response.text}")
            return False
            
    except requests.exceptions.RequestException as e:
        print(f"❌ 네트워크 오류: {product_data['productName']} - {str(e)}")
        return False

def insert_account(account_data):
    """단일 계좌 데이터를 API에 삽입"""
    try:
        response = requests.post(
            ACCOUNTS_URL,
            json=account_data,
            headers={'Content-Type': 'application/json'},
            timeout=10
        )
        
        if response.status_code == 201:
            print(f"✅ 성공: 계좌번호 {account_data['accountNumber']} (고객ID: {account_data['userId']})")
            return True
        else:
            print(f"❌ 실패: 계좌번호 {account_data['accountNumber']} - 상태코드: {response.status_code}")
            print(f"   응답: {response.text}")
            return False
            
    except requests.exceptions.RequestException as e:
        print(f"❌ 네트워크 오류: 계좌번호 {account_data['accountNumber']} - {str(e)}")
        return False

def main():
    """메인 실행 함수"""
    print("🚀 HANA 샘플 데이터 삽입을 시작합니다...")
    print(f"📡 금융상품 API: {PRODUCTS_URL}")
    print(f"📡 계좌 API: {ACCOUNTS_URL}")
    print(f"📊 삽입할 금융상품: {len(sample_products)}개")
    print(f"📊 삽입할 계좌: {len(sample_accounts)}개")
    print("-" * 60)
    
    # 금융상품 삽입
    print("🏦 금융상품 데이터 삽입 중...")
    product_success = 0
    product_failed = 0
    
    for i, product in enumerate(sample_products, 1):
        print(f"[{i}/{len(sample_products)}] 상품 삽입 중...")
        
        if insert_product(product):
            product_success += 1
        else:
            product_failed += 1
        
        # API 부하 방지를 위한 대기
        if i < len(sample_products):
            time.sleep(0.5)
    
    print("-" * 60)
    
    # 계좌 삽입
    print("💳 계좌 데이터 삽입 중...")
    account_success = 0
    account_failed = 0
    
    for i, account in enumerate(sample_accounts, 1):
        print(f"[{i}/{len(sample_accounts)}] 계좌 삽입 중...")
        
        if insert_account(account):
            account_success += 1
        else:
            account_failed += 1
        
        # API 부하 방지를 위한 대기
        if i < len(sample_accounts):
            time.sleep(0.5)
    
    print("-" * 60)
    print(f"📈 삽입 완료!")
    print(f"🏦 금융상품 - ✅ 성공: {product_success}개, ❌ 실패: {product_failed}개")
    print(f"💳 계좌 - ✅ 성공: {account_success}개, ❌ 실패: {account_failed}개")
    
    if product_success > 0 or account_success > 0:
        print(f"\n🔍 삽입된 데이터 확인:")
        print(f"   금융상품: GET {PRODUCTS_URL}")
        print(f"   계좌: GET {ACCOUNTS_URL}")
        print(f"   또는 브라우저에서:")
        print(f"   - http://localhost:8080/api/hana/products")
        print(f"   - http://localhost:8080/api/hana/accounts")

if __name__ == "__main__":
    main()
