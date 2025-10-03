/**
 * 펀드 관련 타입 정의
 */

// 펀드 클래스 상세 정보
export interface FundClassDetail {
  childFundCd: string;
  classCode: string;
  saleStatus: string;
  fundMaster: FundMaster;
  rules: FundRules | null;
  fees: FundFees | null;
  latestNav: number | null;
  latestNavDate: string | null;
}

// 모펀드 정보
export interface FundMaster {
  fundCd: string;
  fundName: string;
  fundGb: number;
  assetType: string;
  riskGrade: string | null;
  currency: string;
  isActive: boolean;
}

// 펀드 규칙
export interface FundRules {
  cutoffTime: string | null;
  navPublishTime: string | null;
  buySettleDays: number | null;
  redeemSettleDays: number | null;
  unitType: string;
  minInitialAmount: number | null;
  minAdditional: number | null;
  incrementAmount: number | null;
  allowSip: boolean;
  allowSwitch: boolean;
  redemptionFeeRate: number | null;
  redemptionFeeDays: number | null;
}

// 펀드 수수료
export interface FundFees {
  salesFeeBps: number | null;
  mgmtFeeBps: number | null;
  trusteeFeeBps: number | null;
  adminFeeBps: number | null;
  totalFeeBps: number | null;
  frontLoadPct: number | null;
}

// 펀드 기준가
export interface FundNav {
  childFundCd: string;
  navDate: string;
  nav: number;
}

// 펀드 매수 요청
export interface FundPurchaseRequest {
  userId: number;
  childFundCd: string;
  purchaseAmount: number;
}

// 펀드 매수 응답
export interface FundPurchaseResponse {
  success: boolean;
  message: string;
  errorMessage?: string;
  subscriptionId?: number;
  childFundCd?: string;
  fundName?: string;
  classCode?: string;
  purchaseAmount?: number;
  purchaseNav?: number;
  purchaseUnits?: number;
  purchaseFee?: number;
  purchaseDate?: string;
  settlementDate?: string;
  irpAccountNumber?: string;
  irpBalanceAfter?: number;
}

// 펀드 매도 요청
export interface FundRedemptionRequest {
  userId: number;
  subscriptionId: number;
  sellUnits?: number;
  sellAll?: boolean;
}

// 펀드 매도 응답
export interface FundRedemptionResponse {
  success: boolean;
  message: string;
  errorMessage?: string;
  subscriptionId?: number;
  childFundCd?: string;
  fundName?: string;
  classCode?: string;
  sellUnits?: number;
  sellNav?: number;
  sellAmount?: number;
  redemptionFee?: number;
  netAmount?: number;
  profit?: number;
  profitRate?: number;
  remainingUnits?: number;
  status?: string;
  redemptionDate?: string;
  settlementDate?: string;
  irpAccountNumber?: string;
  irpBalanceAfter?: number;
}

// 펀드 가입 정보
export interface FundSubscription {
  subscriptionId: number;
  customerCi: string;
  irpAccountNumber: string;
  fundCode: string;
  childFundCd: string;  // 클래스 펀드 코드 (실제 매수 코드)
  fundName: string;
  classCode: string;
  fundType: string;
  riskLevel: string;
  purchaseDate: string;
  purchaseNav: number;
  purchaseAmount: number;
  purchaseFee: number;
  purchaseUnits: number;
  currentUnits: number;
  currentNav: number;
  currentValue: number;
  totalReturn: number;
  returnRate: number;
  accumulatedFees: number;
  status: string;
  bankName: string;
  bankCode: string;
  createdAt: string;
  updatedAt: string;
}

// 펀드 거래 내역
export interface FundTransaction {
  transactionId: number;
  customerCi: string;
  subscriptionId: number;
  childFundCd: string;
  fundName: string;
  classCode: string;
  transactionType: string;
  transactionTypeName: string;
  transactionDate: string;
  settlementDate: string;
  nav: number;
  units: number;
  amount: number;
  fee: number;
  feeType: string;
  profit?: number;
  profitRate?: number;
  irpAccountNumber: string;
  irpBalanceBefore: number;
  irpBalanceAfter: number;
  status: string;
  note: string;
}

// 펀드 거래 통계
export interface FundTransactionStats {
  customerCi: string;
  totalPurchaseCount: number;
  totalPurchaseAmount: number;
  totalPurchaseFee: number;
  totalRedemptionCount: number;
  totalRedemptionAmount: number;
  totalRedemptionFee: number;
  totalRealizedProfit: number;
  totalFees: number;
  totalTransactionCount: number;
  netCashFlow: number;
}

