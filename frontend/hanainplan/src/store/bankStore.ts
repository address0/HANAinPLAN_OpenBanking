import { create } from 'zustand';

interface BankPatterns {
  name: string;
  code: string;
  patterns: string[];
}

interface BankStore {
  bankPatterns: BankPatterns[];
}

export const useBankStore = create<BankStore>((_) => ({
  bankPatterns: [
      {
        name: "하나은행",
        code: "081",
        patterns: [
          "110", "111", "112", "113", "114", "115", "116", "117", "118", "119",
        ],
      },
      {
        name: "국민은행",
        code: "004",
        patterns: ["123", "124", "125", "126", "127", "128", "129"],
      },
      {
        name: "신한은행",
        code: "088",
        patterns: ["456", "457", "458", "459"]
      },
      {
        name: "우리은행",
        code: "020",
        patterns: [
          "100", "101", "102", "103", "104", "105", "106", "107", "108", "109",
        ],
      },
      {
        name: "기업은행",
        code: "003",
        patterns: ["003", "004", "005", "006", "007", "008", "009"],
      },
      {
        name: "NH농협",
        code: "011",
        patterns: ["301", "302", "303", "304", "305", "306", "307", "308", "309"],
      },
      {
        name: "카카오뱅크",
        code: "090",
        patterns: ["333"]
      },
      {
        name: "토스뱅크",
        code: "092",
        patterns: ["092"]
      },
  ],
}));

export const getBankPattern = (bankCode: string) => {
  return useBankStore.getState().bankPatterns.find((bank) => bank.code === bankCode);
};

export const getBankPatternByName = (bankName: string) => {
  return useBankStore.getState().bankPatterns.find((bank) => bank.name === bankName);
};

export const getBankPatternByPattern = (pattern: string) => {
  return useBankStore.getState().bankPatterns.find((bank) => bank.patterns.includes(pattern));
};

/**
 * 계좌번호에서 은행을 찾는 함수
 * @param accountNumber 계좌번호 (예: "081-01-123456" 또는 "08101123456")
 * @returns 은행 정보 또는 null
 */
export const getBankByAccountNumber = (accountNumber: string) => {
  if (!accountNumber) return null;
  
  // 하이픈 제거하고 숫자만 추출
  const cleanAccountNumber = accountNumber.replace(/-/g, '');
  
  // 앞자리 3자리 추출
  const prefix = cleanAccountNumber.substring(0, 3);
  
  const bankPatterns = useBankStore.getState().bankPatterns;
  
  // 1. 먼저 은행 코드로 찾기 (예: 081, 004, 088)
  const bankByCode = bankPatterns.find((bank) => bank.code === prefix);
  if (bankByCode) {
    return bankByCode;
  }
  
  // 2. 패턴으로 찾기 (예: 110, 123, 456)
  const bankByPattern = bankPatterns.find((bank) => bank.patterns.includes(prefix));
  if (bankByPattern) {
    return bankByPattern;
  }
  
  return null;
};
