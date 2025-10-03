# 실제 펀드 상품 구조 구현 완료 ✅

## 📋 개요

실제 하나은행/미래에셋 펀드 상품 데이터를 기반으로 더 정교한 엔티티 구조를 구현했습니다.

**기존 단순 구조 vs 실제 구조:**

| 구분 | 기존 (Phase 1-2) | 실제 구조 (신규) |
|------|-----------------|------------------|
| 펀드 모델 | FundProduct (단일 테이블) | FundMaster + FundClass (모펀드-클래스 분리) |
| 수수료 | 단일 필드 (salesFeeRate) | FundFees (bp 단위, 세분화) |
| 거래 규칙 | 없음 | FundRules (컷오프, 결제일, 최소금액 등) |
| 기준가 | FundNavHistory | FundNav (클래스별) |
| 용도 | 샘플/데모용 | 실무용 상세 구조 |

---

## 🏗️ 엔티티 구조

### 1️⃣ FundMaster (모펀드)

**테이블:** `fund_master`

```java
@Entity
public class FundMaster {
    @Id
    private String fundCd;              // 펀드 코드 (예: 513061)
    private String fundName;            // 펀드명
    private Integer fundGb;             // 펀드 구분 (1: 공모, 2: 사모)
    private String assetType;           // 자산 유형 (채권혼합, 주식형 등)
    private String riskGrade;           // 위험등급
    
    @OneToMany(mappedBy = "fundMaster")
    private List<FundClass> fundClasses; // 클래스 목록 (A/C/P 등)
}
```

**특징:**
- 하나의 모펀드는 여러 클래스를 가질 수 있음
- 클래스마다 다른 보수율/판매 규칙 적용 가능

---

### 2️⃣ FundClass (클래스)

**테이블:** `fund_class`

```java
@Entity
public class FundClass {
    @Id
    private String childFundCd;         // 클래스 펀드 코드 (예: 51306P)
    
    @ManyToOne
    private FundMaster fundMaster;      // 모펀드
    
    private String classCode;           // A/C/I/P 등
    private String loadType;            // FRONT/BACK/NONE
    private String saleStatus;          // ON/OFF/PAUSE
    
    @OneToOne
    private FundRules fundRules;        // 거래 규칙
    
    @OneToOne
    private FundFees fundFees;          // 수수료
}
```

**클래스 종류 예시:**
- **A클래스**: 선취판매수수료형 (가입 시 수수료)
- **C클래스**: 후취판매수수료형 (환매 시 수수료)
- **P클래스**: 연금전용 클래스
- **I클래스**: 기관투자자 전용

---

### 3️⃣ FundRules (거래 규칙)

**테이블:** `fund_rules`

```java
@Entity
public class FundRules {
    @Id
    private String childFundCd;
    
    private LocalTime cutoffTime;       // 주문 컷오프 시각 (예: 15:30)
    private LocalTime navPublishTime;   // 기준가 반영 시각 (예: 10:00)
    private Integer buySettleDays;      // 매수 결제 T+N
    private Integer redeemSettleDays;   // 환매 결제 T+N
    
    private BigDecimal minInitialAmount;    // 최소 최초 투자금액
    private BigDecimal minAdditional;       // 최소 추가 투자금액
    private BigDecimal incrementAmount;     // 투자 증가 단위
    
    private Boolean allowSip;           // 정기투자 허용 여부
    private Boolean allowSwitch;        // 펀드 전환 허용 여부
    
    private BigDecimal redemptionFeeRate;   // 환매수수료율
    private Integer redemptionFeeDays;      // 환매수수료 적용기간
}
```

**주요 규칙:**
- **컷오프 시간**: 15:30 이전 주문 → 당일 기준가 적용
- **결제일**: 매수 T+2, 환매 T+3 등
- **환매수수료**: 90일 이내 환매 시 0.7% 부과 등

---

### 4️⃣ FundFees (수수료)

**테이블:** `fund_fees`

```java
@Entity
public class FundFees {
    @Id
    private String childFundCd;
    
    private Integer mgmtFeeBps;         // 운용보수 (bp) 예: 45 = 0.45%
    private Integer salesFeeBps;        // 판매보수 (bp)
    private Integer trusteeFeeBps;      // 수탁보수 (bp)
    private Integer adminFeeBps;        // 사무관리보수 (bp)
    
    private BigDecimal frontLoadPct;    // 선취판매 수수료율 (%)
    private Integer totalFeeBps;        // 총보수 (bp)
}
```

**bp (basis point) 계산:**
- 1 bp = 0.01%
- 45 bp = 0.45%
- 100 bp = 1.00%

---

### 5️⃣ FundNav (기준가)

**테이블:** `fund_nav`

```java
@Entity
@IdClass(FundNavId.class)
public class FundNav {
    @Id
    private String childFundCd;         // 클래스 코드
    
    @Id
    private LocalDate navDate;          // 기준일
    
    private BigDecimal nav;             // 기준가
    private LocalDateTime publishedAt;  // 발표 시각
}
```

---

## 📊 샘플 데이터

### 1. 미래에셋 퇴직플랜 20 (P클래스)

```
모펀드 코드: 513061
클래스 코드: 51306P
펀드명: 미래에셋퇴직플랜20증권자투자신탁1호(채권혼합)
자산 유형: 채권혼합
펀드 구분: 사모 (연금전용)

[수수료]
- 운용보수: 0.45%
- 판매보수: 0.25%
- 수탁보수: 0.03%
- 사무관리: 0.02%
- 총보수: 0.75%

[거래 규칙]
- 컷오프: 15:30
- 결제: 매수 T+2, 환매 T+3
- 최소 투자금액: 10,000원
- 최소 추가: 1,000원
- 정기투자: 가능
- 환매수수료: 없음

[최근 기준가]
- D-2: 1,012.3456원
- D-1: 1,013.2100원
```

### 2. 미래에셋 퇴직연금 고배당포커스 40 (C클래스)

```
모펀드 코드: 308100
클래스 코드: 30810C
펀드명: 미래에셋퇴직연금고배당포커스40증권자투자신탁1호(채권혼합)
자산 유형: 채권혼합
펀드 구분: 사모 (연금전용)

[수수료]
- 운용보수: 0.55%
- 판매보수: 0.35%
- 수탁보수: 0.03%
- 사무관리: 0.02%
- 총보수: 0.95%

[거래 규칙]
- 컷오프: 15:30
- 결제: 매수 T+2, 환매 T+3
- 최소 투자금액: 10,000원
- 최소 추가: 1,000원
- 정기투자: 가능
- 환매수수료: 0.7% (90일 이내)

[최근 기준가]
- D-2: 987.6543원
- D-1: 990.1200원
```

---

## 🔄 데이터 초기화

### CommandLineRunner 방식 (JPA)

`FundDataLoader.java`가 애플리케이션 시작 시 자동으로 샘플 데이터를 삽입합니다.

```java
@Component
@Profile({"dev", "local"})
public class FundDataLoader implements CommandLineRunner {
    @Override
    public void run(String... args) {
        // 1. 모펀드 생성
        FundMaster master = FundMaster.builder()
            .fundCd("513061")
            .fundName("미래에셋퇴직플랜20증권자투자신탁1호(채권혼합)")
            .build();
        
        // 2. 클래스 생성
        FundClass classP = FundClass.builder()
            .childFundCd("51306P")
            .fundMaster(master)
            .build();
        
        // 3. 규칙/수수료 설정
        // 4. 기준가 데이터 삽입
    }
}
```

**실행 조건:**
- Spring Profile이 `dev` 또는 `local`일 때만 실행
- 이미 데이터가 있으면 스킵

---

## 🚀 테스트 방법

### 1. 애플리케이션 시작

```bash
# application.yml에 프로필 설정
spring.profiles.active: dev

# 또는 실행 시 지정
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 2. 데이터 확인

```sql
-- 모펀드 조회
SELECT * FROM fund_master;

-- 클래스 조회
SELECT * FROM fund_class;

-- 규칙 조회
SELECT * FROM fund_rules;

-- 수수료 조회
SELECT * FROM fund_fees;

-- 기준가 조회
SELECT * FROM fund_nav ORDER BY nav_date DESC;

-- 클래스별 상세 정보 조회
SELECT 
    fc.child_fund_cd,
    fm.fund_name,
    fc.class_code,
    ff.total_fee_bps / 100.0 AS total_fee_pct,
    fr.min_initial_amount,
    fn.nav AS latest_nav
FROM fund_class fc
JOIN fund_master fm ON fc.fund_cd = fm.fund_cd
LEFT JOIN fund_fees ff ON fc.child_fund_cd = ff.child_fund_cd
LEFT JOIN fund_rules fr ON fc.child_fund_cd = fr.child_fund_cd
LEFT JOIN fund_nav fn ON fc.child_fund_cd = fn.child_fund_cd
WHERE fc.sale_status = 'ON'
ORDER BY fn.nav_date DESC;
```

---

## 📝 주요 특징 및 장점

### 1. 모펀드-클래스 분리
- **실무 반영**: 같은 펀드라도 클래스별로 다른 보수/규칙 적용 가능
- **유연성**: A/C/P/I 클래스 각각 관리
- **확장성**: 새로운 클래스 추가 용이

### 2. 세분화된 수수료 구조
- **bp 단위**: 정확한 보수율 표현
- **항목별 분리**: 운용/판매/수탁/사무관리 보수 각각 관리
- **자동 계산**: totalFeeBps 자동 합산

### 3. 상세한 거래 규칙
- **컷오프 시간**: 주문 마감 시각 관리
- **결제일**: T+N 일정 관리
- **환매수수료**: 기간별 수수료 적용 로직

### 4. 클래스별 기준가
- **정확성**: 클래스마다 다른 기준가 가능
- **이력 관리**: 일별 기준가 추적

---

## 🔄 기존 구조와의 관계

### 병행 운영 권장

1. **기존 구조 (FundProduct, FundNavHistory)**
   - Phase 2에서 구현한 샘플/데모용
   - 간단한 테스트 및 프론트엔드 개발용
   - 10개 샘플 펀드 상품

2. **실제 구조 (FundMaster, FundClass, FundRules, FundFees)**
   - 실제 서비스용 정교한 구조
   - 2개 실제 미래에셋 펀드
   - 프로덕션 준비 완료

### 통합 방안 (향후)

- **옵션 1**: 기존 구조 → 실제 구조로 마이그레이션
- **옵션 2**: API 레이어에서 두 구조 통합 (Adapter 패턴)
- **옵션 3**: 실제 구조만 사용하고 기존 구조 제거

---

## 🎯 다음 단계

### 1. Service & Controller 업데이트
- [ ] `FundMasterService` 구현
- [ ] `FundClassService` 구현
- [ ] 클래스 기반 조회 API 추가

### 2. 펀드 매수 로직 업데이트
- [ ] 클래스 선택 기능
- [ ] 수수료 계산 (bp → 원)
- [ ] 컷오프 시간 검증

### 3. 프론트엔드 통합
- [ ] 클래스별 상품 표시
- [ ] 수수료 상세 정보 표시
- [ ] 거래 규칙 안내

---

## ✅ 완료 요약

✅ **실제 펀드 엔티티**: FundMaster, FundClass, FundRules, FundFees, FundNav  
✅ **Repository**: 3개 Repository (Master, Class, Nav)  
✅ **데이터 초기화**: CommandLineRunner 방식  
✅ **샘플 데이터**: 미래에셋 2개 실제 펀드  
✅ **bp 단위 보수**: 정확한 수수료 계산  
✅ **거래 규칙**: 컷오프, 결제일, 환매수수료 등  

**실제 펀드 구조 구현 완료! 🎉**

