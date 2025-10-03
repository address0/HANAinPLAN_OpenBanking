/**
 * 펀드 관련 유틸리티 함수
 */

/**
 * 금액 포맷팅 (원 단위, 소수점 없음)
 * @param num 금액
 * @returns 포맷팅된 문자열 (예: "1,234,567원")
 */
export const formatAmount = (num: number | null | undefined): string => {
  if (num === null || num === undefined) return '-';
  // 소수점 버림 처리 후 포맷팅
  return Math.floor(num).toLocaleString('ko-KR');
};

/**
 * 좌수 포맷팅 (소수점 6자리)
 * @param units 좌수
 * @returns 포맷팅된 문자열 (예: "1,234.567890좌")
 */
export const formatUnits = (units: number | null | undefined): string => {
  if (units === null || units === undefined) return '-';
  return units.toFixed(6);
};

/**
 * 퍼센트 포맷팅 (BPS → %)
 * @param bps Basis Points (1/100 %)
 * @returns 포맷팅된 문자열 (예: "1.25%")
 */
export const formatPercent = (bps: number | null | undefined): string => {
  if (bps === null || bps === undefined) return '-';
  return `${(bps / 100).toFixed(2)}%`;
};

/**
 * 수익률 포맷팅
 * @param rate 수익률 (%)
 * @returns 포맷팅된 문자열 (예: "+12.34%")
 */
export const formatReturnRate = (rate: number | null | undefined): string => {
  if (rate === null || rate === undefined) return '-';
  const sign = rate >= 0 ? '+' : '';
  return `${sign}${rate.toFixed(2)}%`;
};

/**
 * 수익률에 따른 텍스트 색상 클래스 반환
 * @param rate 수익률
 * @returns Tailwind CSS 클래스명
 */
export const getReturnColor = (rate: number): string => {
  if (rate > 0) return 'text-red-600';
  if (rate < 0) return 'text-blue-600';
  return 'text-gray-600';
};

/**
 * 수익률에 따른 배경 색상 클래스 반환
 * @param rate 수익률
 * @returns Tailwind CSS 클래스명
 */
export const getReturnBgColor = (rate: number): string => {
  if (rate > 0) return 'bg-red-50';
  if (rate < 0) return 'bg-blue-50';
  return 'bg-gray-50';
};

/**
 * 위험등급에 따른 색상 클래스 반환
 * @param riskGrade 위험등급 (1~6)
 * @returns Tailwind CSS 클래스명
 */
export const getRiskColor = (riskGrade: string): string => {
  const riskNum = parseInt(riskGrade);
  if (riskNum <= 2) return 'text-blue-600';
  if (riskNum <= 4) return 'text-green-600';
  return 'text-red-600';
};

/**
 * 거래 유형에 따른 색상 클래스 반환
 * @param type 거래 유형 (BUY, SELL, DIVIDEND)
 * @returns Tailwind CSS 클래스명
 */
export const getTransactionTypeColor = (type: string): string => {
  switch (type) {
    case 'BUY':
      return 'bg-blue-100 text-blue-800';
    case 'SELL':
      return 'bg-red-100 text-red-800';
    case 'DIVIDEND':
      return 'bg-green-100 text-green-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
};

