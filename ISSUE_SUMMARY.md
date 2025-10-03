# IRP 계좌 개설 문제 분석 및 해결 방안

## 발견된 문제들

### 1. IRP 계좌가 일반 계좌 테이블(tb_banking_account)에도 생성됨
**원인**: `IrpIntegrationServiceImpl.createIrpAccountOpenTransactions()` 메서드 Line 630-643에서 IRP 가상 계좌를 `BankingAccount`로 생성

**해결**: IRP 계좌는 `tb_irp_account` 테이블에만 저장하고, 일반 계좌 테이블에는 생성하지 않도록 수정

### 2. 100만원이 100원으로 인식됨
**원인**: 
- 하나인플랜 → 하나은행 요청 시 Line 525에서 금액을 만원 단위로 변환 (1,000,000원 → 100만원)
- 하나은행 서버가 이를 다시 원 단위로 변환하지 않고 그대로 100원으로 저장

**해결**: 단위 변환 로직 제거 또는 양쪽 서버의 단위 통일

### 3. 하나인플랜 IRP 계좌 잔액이 0원
**원인**: Line 187에서 `initialDeposit`만 설정하고 `currentBalance`를 설정하지 않음

**해결**: IRP 계좌 생성 시 `currentBalance`를 `initialDeposit`과 동일하게 설정

### 4. 하나은행에 거래내역 생성 안됨
**원인**: 하나은행 IRP 계좌 개설 API에서 거래내역 생성 로직 누락

**해결**: 하나은행 IRP 계좌 개설 시 거래내역 생성 로직 추가

### 5. Kafka 동기화 실패
**원인**: 
- 메시지 형식 불일치
- 하나인플랜 IRP 계좌의 잔액이 0원이므로 동기화해도 의미 없음

**해결**: 위 문제들을 해결한 후 Kafka 동기화 확인



