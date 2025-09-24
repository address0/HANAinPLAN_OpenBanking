# HANA 샘플 데이터 삽입

이 디렉토리에는 HANA 금융상품과 계좌 테이블에 샘플 데이터를 자동으로 삽입하는 Python 스크립트가 포함되어 있습니다.

## 파일 설명

- `insert_sample_data.py`: 금융상품과 계좌 샘플 데이터를 자동으로 삽입하는 Python 스크립트

## 사용 방법

### 1. 사전 준비

1. HANA 은행 서버가 실행 중인지 확인
   ```bash
   # 서버가 localhost:8080에서 실행 중이어야 함
   curl http://localhost:8080/api/hana/products
   curl http://localhost:8080/api/hana/accounts
   ```

2. Python 3.x가 설치되어 있는지 확인
   ```bash
   python3 --version
   ```

3. requests 라이브러리 설치 (필요한 경우)
   ```bash
   pip3 install requests
   ```

### 2. 스크립트 실행

```bash
# 방법 1: 직접 실행
python3 insert_sample_data.py

# 방법 2: 실행 권한이 있는 경우
./insert_sample_data.py
```

### 3. 실행 결과 확인

스크립트 실행 후 다음 방법으로 데이터가 정상적으로 삽입되었는지 확인할 수 있습니다:

```bash
# API로 확인
curl http://localhost:8080/api/hana/products
curl http://localhost:8080/api/hana/accounts

# 또는 브라우저에서 접속
http://localhost:8080/api/hana/products
http://localhost:8080/api/hana/accounts
```

## 삽입되는 샘플 데이터

### 금융상품 데이터 (10개)

스크립트는 다음과 같은 10개의 금융상품 데이터를 삽입합니다:

1. **하나 정기예금** (HANA001) - 정기예금, 6-24개월
2. **하나 적금** (HANA002) - 적금, 12-36개월
3. **하나 자유적금** (HANA003) - 자유적금, 6-60개월
4. **하나 청년정기예금** (HANA004) - 청년 대상 정기예금, 12-36개월
5. **하나 주택청약종합저축** (HANA005) - 주택청약저축, 24-120개월
6. **하나 연금저축** (HANA006) - 연금저축, 60-240개월
7. **하나 우대정기예금** (HANA007) - 우대금리 정기예금, 12-24개월
8. **하나 마이너스통장** (HANA008) - 마이너스통장, 12-60개월
9. **하나 전세자금대출** (HANA009) - 전세자금대출, 24-120개월
10. **하나 신용대출** (HANA010) - 신용대출, 12-84개월

### 계좌 데이터 (10개)

스크립트는 다음과 같은 10개의 계좌 데이터를 삽입합니다:

1. **1001234567890** - 수시입출금, 잔액 50만원 (고객ID: 1)
2. **1001234567891** - 예적금, 잔액 100만원 (고객ID: 1)
3. **1001234567892** - 수시입출금, 잔액 250만원 (고객ID: 2)
4. **1001234567893** - 예적금, 잔액 500만원 (고객ID: 2)
5. **1001234567894** - 수시입출금, 잔액 75만원 (고객ID: 3)
6. **1001234567895** - 수익증권, 잔액 200만원 (고객ID: 3)
7. **1001234567896** - 수시입출금, 잔액 120만원 (고객ID: 4)
8. **1001234567897** - 예적금, 잔액 300만원 (고객ID: 4)
9. **1001234567898** - 수시입출금, 잔액 180만원 (고객ID: 5)
10. **1001234567899** - 통합계좌, 잔액 800만원 (고객ID: 5)

## 데이터 구조

각 금융상품 데이터는 다음 필드들을 포함합니다:

- `productCode`: 상품코드 (고유값)
- `productName`: 상품명
- `depositType`: 예금종류
- `minContractPeriod`: 가입기간(최소) - 숫자 (개월 단위)
- `maxContractPeriod`: 가입기간(최대) - 숫자 (개월 단위)
- `contractPeriodUnit`: 가입기간 단위
- `subscriptionTarget`: 가입대상
- `subscriptionAmount`: 가입금액 - 숫자 (원 단위)
- `productCategory`: 상품유형
- `interestPayment`: 이자지급
- `taxBenefit`: 세제혜택
- `partialWithdrawal`: 일부해지
- `cancellationPenalty`: 해지 시 불이익
- `description`: 상품 설명

각 계좌 데이터는 다음 필드들을 포함합니다:

- `accountNumber`: 계좌번호 (고유값)
- `accountType`: 계좌종류 (1: 수시입출금, 2: 예적금, 6: 수익증권, 0: 통합계좌)
- `balance`: 잔액
- `openingDate`: 계좌개설일
- `maturityDate`: 만기일 (예적금의 경우)
- `thirdPartyConsent`: 제3자정보제공동의여부
- `withdrawalConsent`: 출금동의여부
- `userId`: 고객 ID

## 주의사항

- 서버가 실행 중이지 않으면 스크립트가 실패합니다
- 동일한 상품코드나 계좌번호가 이미 존재하는 경우 중복 오류가 발생할 수 있습니다
- 계좌 생성 시 고객 ID가 존재해야 합니다 (userId: 1-5)
- 네트워크 연결이 불안정한 경우 일부 데이터 삽입이 실패할 수 있습니다

## 문제 해결

### 서버 연결 오류
```
❌ 네트워크 오류: Connection refused
```
- HANA 은행 서버가 실행 중인지 확인
- 포트 8080이 사용 가능한지 확인

### 중복 데이터 오류
```
❌ 실패: 이미 존재하는 상품코드입니다
❌ 실패: 이미 존재하는 계좌번호입니다
```
- 기존 데이터를 삭제하거나 다른 상품코드/계좌번호를 사용

### 고객 ID 오류
```
❌ 실패: 고객을 찾을 수 없습니다
```
- 계좌 생성 전에 고객 데이터가 존재하는지 확인
- userId 1-5에 해당하는 고객 데이터가 필요

### 권한 오류
```
Permission denied
```
- 스크립트에 실행 권한 부여: `chmod +x insert_sample_data.py`
