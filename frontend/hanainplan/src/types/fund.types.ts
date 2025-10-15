
export interface FundClassDetail {
  childFundCd: string;
  classCode: string;
  saleStatus: string;
  sourceUrl: string | null;
  fundMaster: FundMaster;
  rules: FundRules | null;
  fees: FundFees | null;
  latestNav: number | null;
  latestNavDate: string | null;

  return1month?: number;
  return3month?: number;
  return6month?: number;
  return1year?: number;
  return3year?: number;
}

export interface FundMaster {
  fundCd: string;
  fundName: string;
  fundGb: number;
  assetType: string;
  riskGrade: string | null;
  currency: string;
  isActive: boolean;
  managementCompany?: string;
  trustCompany?: string;
}

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

export interface FundFees {
  salesFeeBps: number | null;
  mgmtFeeBps: number | null;
  trusteeFeeBps: number | null;
  adminFeeBps: number | null;
  totalFeeBps: number | null;
  frontLoadPct: number | null;
}

export interface FundNav {
  childFundCd: string;
  navDate: string;
  nav: number;
}

export interface FundPurchaseRequest {
  userId: number;
  childFundCd: string;
  purchaseAmount: number;
}

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

export interface FundRedemptionRequest {
  userId: number;
  subscriptionId: number;
  sellUnits?: number;
  sellAll?: boolean;
}

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

export interface FundSubscription {
  subscriptionId: number;
  customerCi: string;
  irpAccountNumber: string;
  fundCode: string;
  childFundCd: string;
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