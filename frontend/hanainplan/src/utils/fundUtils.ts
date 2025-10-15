
export const formatAmount = (num: number | null | undefined): string => {
  if (num === null || num === undefined) return '-';
  return Math.floor(num).toLocaleString('ko-KR');
};

export const formatUnits = (units: number | null | undefined): string => {
  if (units === null || units === undefined) return '-';
  return units.toFixed(6);
};

export const formatPercent = (bps: number | null | undefined): string => {
  if (bps === null || bps === undefined) return '-';
  return `${(bps / 100).toFixed(2)}%`;
};

export const formatReturnRate = (rate: number | null | undefined): string => {
  if (rate === null || rate === undefined) return '-';
  const sign = rate >= 0 ? '+' : '';
  return `${sign}${rate.toFixed(2)}%`;
};

export const getReturnColor = (rate: number): string => {
  if (rate > 0) return 'text-red-600';
  if (rate < 0) return 'text-blue-600';
  return 'text-gray-600';
};

export const getReturnBgColor = (rate: number): string => {
  if (rate > 0) return 'bg-red-50';
  if (rate < 0) return 'bg-blue-50';
  return 'bg-gray-50';
};

export const getRiskColor = (riskGrade: string): string => {
  const riskNum = parseInt(riskGrade);
  if (riskNum <= 2) return 'text-blue-600';
  if (riskNum <= 4) return 'text-green-600';
  return 'text-red-600';
};

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