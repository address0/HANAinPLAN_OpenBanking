import { httpGet, httpPost } from '../lib/http';

// 리밸런싱 시뮬레이션 요청 타입
export interface RebalancingSimulationRequest {
  customerId: number;
  triggerType: 'MANUAL' | 'TIME_BASED' | 'BAND_BASED';
  cashWeight?: number;
  depositWeight?: number;
  fundWeight?: number;
}

// 포트폴리오 스냅샷 타입
export interface PortfolioSnapshot {
  totalValue: number;
  cashWeight: number;
  depositWeight: number;
  fundWeight: number;
  cashAmount: number;
  depositAmount: number;
  fundAmount: number;
}

// 리밸런싱 주문 타입
export interface RebalancingOrder {
  orderType: 'BUY' | 'SELL';
  assetType: 'FUND';
  fundCode: string;
  fundName: string;
  orderAmount: number;
  expectedNav: number;
  orderUnits: number;
  fee: number;
  reason: string;
}

// 리밸런싱 시뮬레이션 응답 타입
export interface RebalancingSimulationResponse {
  jobId: number;
  customerId: number;
  irpAccountNumber: string;
  triggerType: string;
  status: string;
  currentPortfolio: PortfolioSnapshot;
  targetPortfolio: PortfolioSnapshot;
  expectedPortfolio: PortfolioSnapshot;
  orders: RebalancingOrder[];
  totalFee: number;
  totalOrderAmount: number;
  message: string;
  createdAt?: string;
}

// IRP 포트폴리오 조회 응답 타입
export interface IrpPortfolioResponse {
  customerId: number;
  irpAccountNumber: string;
  totalValue: number;
  cashBalance: number;
  depositTotal: number;
  fundTotal: number;
  cashWeight: number;
  depositWeight: number;
  fundWeight: number;
  holdings: IrpHolding[];
}

// IRP 보유 자산 타입
export interface IrpHolding {
  assetCode: string;
  assetName: string;
  assetType: 'CASH' | 'DEPOSIT' | 'FUND';
  currentValue: number;
  units?: number;
  nav?: number;
  purchasePrice?: number;
  returnRate?: number;
  maturityDate?: string;
}

/**
 * 리밸런싱 시뮬레이션 실행
 */
export async function simulateRebalancing(
  customerId: number,
  request?: Partial<RebalancingSimulationRequest>
): Promise<RebalancingSimulationResponse> {
  const simulationRequest: RebalancingSimulationRequest = {
    customerId,
    triggerType: 'MANUAL',
    ...request
  };

  return httpPost<RebalancingSimulationResponse>(
    `irp/rebalancing/${customerId}/simulate`,
    simulationRequest
  );
}

/**
 * 추천 포트폴리오 기반 리밸런싱 시뮬레이션
 */
export async function simulateRecommendedRebalancing(
  customerId: number
): Promise<RebalancingSimulationResponse> {
  return httpPost<RebalancingSimulationResponse>(
    `irp/rebalancing/${customerId}/simulate-recommended`
  );
}

/**
 * 사용자 지정 목표 비중으로 리밸런싱 시뮬레이션
 */
export async function simulateCustomRebalancing(
  customerId: number,
  cashWeight: number,
  depositWeight: number,
  fundWeight: number
): Promise<RebalancingSimulationResponse> {
  return httpPost<RebalancingSimulationResponse>(
    `irp/rebalancing/${customerId}/simulate-custom`,
    null,
    {
      params: {
        cashWeight,
        depositWeight,
        fundWeight
      }
    }
  );
}

/**
 * 리밸런싱 실행 승인
 */
export async function approveRebalancing(
  jobId: number
): Promise<RebalancingSimulationResponse> {
  return httpPost<RebalancingSimulationResponse>(
    `irp/rebalancing/${jobId}/approve`
  );
}

/**
 * 리밸런싱 상태 조회
 */
export async function getRebalancingStatus(
  jobId: number
): Promise<RebalancingSimulationResponse> {
  return httpGet<RebalancingSimulationResponse>(
    `irp/rebalancing/${jobId}/status`
  );
}

/**
 * IRP 포트폴리오 조회
 */
export async function getIrpPortfolio(
  customerId: number
): Promise<IrpPortfolioResponse> {
  return httpGet<IrpPortfolioResponse>(
    `irp/portfolio/${customerId}`
  );
}

/**
 * 유사 사용자 포트폴리오 추천 조회
 */
export async function getSimilarUserPortfolio(
  customerId: number
): Promise<{
  customerId: number;
  cashWeight: number;
  depositWeight: number;
  fundWeight: number;
  similarUsers: Array<{
    customerId: number;
    similarity: number;
    age: number;
    assetLevel: string;
    riskProfile: string;
  }>;
}> {
  return httpGet<{
    customerId: number;
    cashWeight: number;
    depositWeight: number;
    fundWeight: number;
    similarUsers: Array<{
      customerId: number;
      similarity: number;
      age: number;
      assetLevel: string;
      riskProfile: string;
    }>;
  }>(`irp/portfolio/${customerId}/similar-user-portfolio`);
}
