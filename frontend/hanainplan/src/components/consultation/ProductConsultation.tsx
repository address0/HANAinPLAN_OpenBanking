import React, { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getAllDepositProducts } from '../../api/productApi';
import { fundProductApi } from '../../api/fundApi';
import { subscribeDepositProduct, type DepositSubscribeRequest } from '../../api/depositSubscriptionApi';
import { purchaseFund, redeemFund, getActiveFundSubscriptions, type FundPortfolio } from '../../api/fundSubscriptionApi';
import { getIrpAccount } from '../../api/portfolioApi';
import NotesTab from './NotesTab';
import WebSocketService from '../../services/WebSocketService';

interface ProductConsultationProps {
  consultationInfo: {
    id?: string;
    type: string;
    detail?: string;
  };
  currentUserId: number;
  currentUserRole: 'customer' | 'counselor';
  targetUserId: number;
  isInCall: boolean;
}

type ProductType = 'deposit' | 'fund';
type TransactionType = 'buy' | 'sell';

interface DepositFormData {
  accountName: string;
  initialBalance: string;
  depositPeriod: number;
  interestPaymentMethod: 'AUTO' | 'MANUAL';
}

interface IrpPasswordData {
  password: string;
}

interface FundFormData {
  transactionType: TransactionType;
  amount: string;
  sellUnits: string;
  sellAll: boolean;
  selectedSubscription?: FundPortfolio;
}

const ProductConsultation: React.FC<ProductConsultationProps> = ({
  consultationInfo,
  currentUserId,
  currentUserRole,
  targetUserId,
  isInCall
}) => {
  const [currentStep, setCurrentStep] = useState<number>(1);
  const [idVerified, setIdVerified] = useState<boolean>(false);

  const [productType, setProductType] = useState<ProductType | null>(null);
  const [productInfo, setProductInfo] = useState<any>(null);

  const [depositFormData, setDepositFormData] = useState<DepositFormData>({
    accountName: '',
    initialBalance: '',
    depositPeriod: 12,
    interestPaymentMethod: 'AUTO'
  });

  const [irpPassword, setIrpPassword] = useState<IrpPasswordData>({
    password: ''
  });

  const [fundFormData, setFundFormData] = useState<FundFormData>({
    transactionType: 'buy',
    amount: '',
    sellUnits: '',
    sellAll: false
  });

  const [isProcessing, setIsProcessing] = useState<boolean>(false);
  const [processResult, setProcessResult] = useState<{success: boolean; message: string} | null>(null);
  const [error, setError] = useState<string>('');

  const [depositProjection, setDepositProjection] = useState<{
    principal: number;
    interest: number;
    total: number;
    monthlyBreakdown: Array<{ month: number; principal: number; interest: number; total: number }>;
  } | null>(null);

  const [fundProjection, setFundProjection] = useState<{
    scenarios: Array<{ rate: number; label: string; finalAmount: number; profit: number }>;
  } | null>(null);

  const [passwordCompleted, setPasswordCompleted] = useState<boolean>(false);

  const { data: depositProducts = [] } = useQuery({
    queryKey: ['depositProducts'],
    queryFn: async () => {
      const response = await getAllDepositProducts();
      return response.products || [];
    }
  });

  const { data: fundClasses = [] } = useQuery({
    queryKey: ['fundClasses'],
    queryFn: async () => {
      return await fundProductApi.getAllFundClasses();
    }
  });

  const { data: interestRates = [] } = useQuery({
    queryKey: ['allInterestRates'],
    queryFn: async () => {
      try {
        const response = await fetch('/api/banking/interest-rates/all');
        if (!response.ok) {
          throw new Error('금리 정보 조회 실패');
        }
        return await response.json();
      } catch (error) {
        return [];
      }
    }
  });

  const targetUserIdForFund = currentUserRole === 'counselor' ? targetUserId : currentUserId;
  const { data: userFundPortfolio = [] } = useQuery({
    queryKey: ['userFundPortfolio', targetUserIdForFund],
    queryFn: async () => {
      if (productType === 'fund' && fundFormData.transactionType === 'sell') {
        return await getActiveFundSubscriptions(targetUserIdForFund);
      }
      return [];
    },
    enabled: productType === 'fund' && fundFormData.transactionType === 'sell'
  });

  const targetUserIdForIrp = currentUserRole === 'counselor' ? targetUserId : currentUserId;
  const { data: irpAccount } = useQuery({
    queryKey: ['irpAccount', targetUserIdForIrp],
    queryFn: () => getIrpAccount(targetUserIdForIrp),
    enabled: !!targetUserIdForIrp && currentStep >= 3 && !!productType
  });

  useEffect(() => {
    if (!consultationInfo.detail || !depositProducts.length || !fundClasses.length) return;

    const depositProduct = depositProducts.find(p => p.name === consultationInfo.detail);
    if (depositProduct) {
      setProductType('deposit');
      setProductInfo(depositProduct);
      setDepositFormData(prev => ({
        ...prev,
        depositPeriod: 12
      }));
      return;
    }

    const fundProduct = fundClasses.find(f => f.fundMaster?.fundName === consultationInfo.detail);
    if (fundProduct) {
      setProductType('fund');
      setProductInfo({
        ...fundProduct.fundMaster,
        classCode: fundProduct.classCode,
        childFundCd: fundProduct.childFundCd
      });
    }
  }, [consultationInfo.detail, depositProducts, fundClasses]);

  useEffect(() => {
    if (currentUserRole === 'customer') {
      WebSocketService.onConsultationStepSync((message) => {
        if (message.data) {
          setCurrentStep(message.data.step || currentStep);
          setIdVerified(message.data.idVerified || idVerified);
          if (message.data.depositFormData) {
            setDepositFormData(prev => ({
              ...prev,
              ...message.data.depositFormData
            }));
          }
          if (message.data.fundFormData) {
            setFundFormData(prev => ({
              ...prev,
              ...message.data.fundFormData
            }));
          }
        }
      });
    } else if (currentUserRole === 'counselor') {
      WebSocketService.onConsultationStepSync((message) => {
        if (message.data && message.data.passwordCompleted !== undefined) {
          setPasswordCompleted(message.data.passwordCompleted);
        }
      });
    }

    return () => {
    };
  }, [currentUserRole, currentStep, idVerified]);

  useEffect(() => {
    if (currentUserRole === 'customer' &&
        consultationInfo.id &&
        currentStep === 3) {
      const isCompleted = irpPassword.password.length === 4 &&
                         /^\d{4}$/.test(irpPassword.password);

      if (isCompleted !== passwordCompleted) {
        setPasswordCompleted(isCompleted);
        WebSocketService.sendConsultationStepSync({
          type: 'CONSULTATION_STEP_SYNC',
          roomId: consultationInfo.id,
          senderId: currentUserId,
          receiverId: targetUserId,
          data: {
            step: currentStep,
            passwordCompleted: isCompleted
          }
        });
      }
    }
  }, [irpPassword.password, currentUserRole, consultationInfo.id, currentStep, currentUserId, targetUserId, passwordCompleted]);

  const canProceedToNextStep = (): boolean => {
    switch (currentStep) {
      case 1:
        return idVerified;
      case 2:
        return true;
      case 3:
        let passwordValid = false;
        if (currentUserRole === 'customer') {
          passwordValid = irpPassword.password.length === 4 && /^\d{4}$/.test(irpPassword.password);
        } else {
          passwordValid = passwordCompleted;
        }

        if (productType === 'deposit') {
          return depositFormData.initialBalance !== '' &&
                 Number(depositFormData.initialBalance) >= 1000000 &&
                 passwordValid;
        } else if (productType === 'fund') {
          if (fundFormData.transactionType === 'buy') {
            return fundFormData.amount !== '' && Number(fundFormData.amount) > 0 && passwordValid;
          } else {
            return (fundFormData.sellAll || (fundFormData.sellUnits !== '' && Number(fundFormData.sellUnits) > 0)) && passwordValid;
          }
        }
        return false;
      case 4:
        return false;
      default:
        return false;
    }
  };

  const findInterestRateForPeriod = (period: number, bankCode: string): number => {
    if (!interestRates || interestRates.length === 0 || !productInfo) {
      return 0.03;
    }

    const relevantRates = interestRates.filter((rate: any) =>
      rate.bankCode === bankCode && rate.interestType === 'BASIC'
    );

    if (relevantRates.length === 0) {
      return 0.03;
    }

    const matchingRate = relevantRates.find((rate: any) => {
      const periodMatch = rate.maturityPeriod?.match(/(\d+)개월/);
      if (periodMatch) {
        const ratePeriod = parseInt(periodMatch[1]);
        return ratePeriod === period;
      }
      return false;
    });

    if (matchingRate) {
      return matchingRate.interestRate;
    }

    const sortedRates = relevantRates
      .map((rate: any) => {
        const periodMatch = rate.maturityPeriod?.match(/(\d+)개월/);
        return {
          period: periodMatch ? parseInt(periodMatch[1]) : 0,
          rate: rate.interestRate
        };
      })
      .filter((r: any) => r.period > 0)
      .sort((a: any, b: any) => Math.abs(a.period - period) - Math.abs(b.period - period));

    return sortedRates.length > 0 ? sortedRates[0].rate : 0.03;
  };

  const calculateDepositProjection = (principal: number, period: number, annualRate: number) => {
    const monthlyRate = annualRate / 12;
    const totalMonths = period;
    const monthlyBreakdown = [];

    let currentPrincipal = principal;
    let totalInterest = 0;

    for (let month = 1; month <= totalMonths; month++) {
      const monthInterest = currentPrincipal * monthlyRate;
      totalInterest += monthInterest;
      currentPrincipal += monthInterest;

      monthlyBreakdown.push({
        month,
        principal,
        interest: totalInterest,
        total: currentPrincipal
      });
    }

    setDepositProjection({
      principal,
      interest: totalInterest,
      total: currentPrincipal,
      monthlyBreakdown
    });
  };

  const calculateFundProjection = (amount: number) => {
    const scenarios = [
      { rate: -0.10, label: '비관적 (-10%)', finalAmount: 0, profit: 0 },
      { rate: 0.05, label: '보수적 (+5%)', finalAmount: 0, profit: 0 },
      { rate: 0.15, label: '낙관적 (+15%)', finalAmount: 0, profit: 0 },
      { rate: 0.25, label: '최상 (+25%)', finalAmount: 0, profit: 0 }
    ];

    scenarios.forEach(scenario => {
      scenario.finalAmount = amount * (1 + scenario.rate);
      scenario.profit = scenario.finalAmount - amount;
    });

    setFundProjection({ scenarios });
  };

  useEffect(() => {
    if (productType === 'deposit' &&
        depositFormData.initialBalance &&
        Number(depositFormData.initialBalance) >= 1000000 &&
        depositFormData.depositPeriod &&
        productInfo) {
      const rate = findInterestRateForPeriod(depositFormData.depositPeriod, productInfo.bankCode || 'HANA');

      calculateDepositProjection(
        Number(depositFormData.initialBalance),
        depositFormData.depositPeriod,
        rate
      );

      if (currentUserRole === 'counselor' && consultationInfo.id && currentStep === 3) {
        WebSocketService.sendConsultationStepSync({
          type: 'CONSULTATION_STEP_SYNC',
          roomId: consultationInfo.id,
          senderId: currentUserId,
          receiverId: targetUserId,
          data: {
            step: currentStep,
            idVerified,
            depositFormData
          }
        });
      }
    }
  }, [depositFormData.initialBalance, depositFormData.depositPeriod, depositFormData.interestPaymentMethod, productType, currentUserRole, currentStep, consultationInfo.id, currentUserId, targetUserId, idVerified, productInfo, interestRates]);

  useEffect(() => {
    if (productType === 'fund' &&
        fundFormData.transactionType === 'buy' &&
        fundFormData.amount &&
        Number(fundFormData.amount) > 0) {
      calculateFundProjection(Number(fundFormData.amount));

      if (currentUserRole === 'counselor' && consultationInfo.id && currentStep === 3) {
        WebSocketService.sendConsultationStepSync({
          type: 'CONSULTATION_STEP_SYNC',
          roomId: consultationInfo.id,
          senderId: currentUserId,
          receiverId: targetUserId,
          data: {
            step: currentStep,
            idVerified,
            fundFormData
          }
        });
      }
    }
  }, [fundFormData, productType, currentUserRole, currentStep, consultationInfo.id, currentUserId, targetUserId, idVerified]);

  const handleNextStep = () => {
    if (canProceedToNextStep()) {
      const newStep = Math.min(currentStep + 1, 4);
      setCurrentStep(newStep);
      setError('');

      if (currentUserRole === 'counselor' && consultationInfo.id) {
        WebSocketService.sendConsultationStepSync({
          type: 'CONSULTATION_STEP_SYNC',
          roomId: consultationInfo.id,
          senderId: currentUserId,
          receiverId: targetUserId,
          data: {
            step: newStep,
            idVerified,
            depositFormData: productType === 'deposit' ? depositFormData : undefined,
            fundFormData: productType === 'fund' ? fundFormData : undefined
          }
        });
      }
    }
  };

  const handlePrevStep = () => {
    const newStep = Math.max(currentStep - 1, 1);
    setCurrentStep(newStep);
    setError('');
    setProcessResult(null);

    if (currentUserRole === 'counselor' && consultationInfo.id) {
      WebSocketService.sendConsultationStepSync({
        type: 'CONSULTATION_STEP_SYNC',
        roomId: consultationInfo.id,
        senderId: currentUserId,
        receiverId: targetUserId,
        data: { step: newStep, idVerified }
      });
    }
  };

  const handleFinalApproval = async () => {
    setIsProcessing(true);
    setError('');

    const customerUserId = currentUserRole === 'counselor' ? targetUserId : currentUserId;

    try {
      if (productType === 'deposit') {
        if (!irpAccount) {
          throw new Error('IRP 계좌 정보를 찾을 수 없습니다.');
        }

        const request: DepositSubscribeRequest = {
          userId: customerUserId,
          bankCode: irpAccount.bankCode,
          irpAccountNumber: irpAccount.accountNumber,
          linkedAccountNumber: irpAccount.accountNumber,
          depositCode: productInfo.depositCode,
          productName: productInfo.name,
          productType: 0,
          contractPeriod: depositFormData.depositPeriod,
          subscriptionAmount: Number(depositFormData.initialBalance)
        };

        const result = await subscribeDepositProduct(request);

        if (result.success) {
        setProcessResult({
          success: true,
            message: `정기예금 상품 가입이 완료되었습니다.\n상품명: ${productInfo.name}\n가입 금액: ${result.subscriptionAmount?.toLocaleString()}원\n가입 기간: ${result.contractPeriod}개월${result.expectedMaturityAmount ? `\n예상 만기금액: ${result.expectedMaturityAmount.toLocaleString()}원` : ''}`
        });
        } else {
          throw new Error(result.message || '예금 상품 가입에 실패했습니다.');
        }

      } else if (productType === 'fund') {
        if (fundFormData.transactionType === 'buy') {
          const result = await purchaseFund({
            userId: customerUserId,
            childFundCd: productInfo.childFundCd,
            purchaseAmount: Number(fundFormData.amount)
          });

          if (result.success) {
            setProcessResult({
              success: true,
              message: `펀드 매수가 완료되었습니다. (매수 좌수: ${result.purchaseUnits?.toFixed(4)})`
            });
          } else {
            throw new Error(result.errorMessage || '펀드 매수에 실패했습니다.');
          }
        } else {
          if (!fundFormData.selectedSubscription) {
            throw new Error('매도할 펀드를 선택해주세요.');
          }

          const result = await redeemFund({
            userId: customerUserId,
            subscriptionId: fundFormData.selectedSubscription.subscriptionId,
            sellUnits: fundFormData.sellAll ? undefined : Number(fundFormData.sellUnits),
            sellAll: fundFormData.sellAll
          });

          if (result.success) {
            setProcessResult({
              success: true,
              message: `펀드 매도가 완료되었습니다. (실수령액: ${result.netAmount?.toLocaleString()}원, 실현 손익: ${result.profit?.toLocaleString()}원)`
            });
          } else {
            throw new Error(result.errorMessage || '펀드 매도에 실패했습니다.');
          }
        }
      }
    } catch (err: any) {
      setError(err.message || '가입 처리 중 오류가 발생했습니다.');
      setProcessResult({
        success: false,
        message: err.message || '가입 처리 중 오류가 발생했습니다.'
      });
    } finally {
      setIsProcessing(false);
    }
  };

  const renderStep1 = () => (
    <div className="space-y-6">
      <h3 className="text-xl font-bold text-gray-900">1단계: 신분증 확인</h3>

      {currentUserRole === 'customer' ? (
        <div className="bg-blue-50 border-2 border-blue-200 rounded-lg p-6 text-center">
          <div className="text-6xl mb-4">🪪</div>
          <h4 className="text-lg font-bold text-gray-900 mb-2">신분증을 카메라에 보여주세요</h4>
          <p className="text-gray-600">
            상담사가 본인 확인을 위해 신분증을 확인합니다.<br />
            주민등록증 또는 운전면허증을 준비해주세요.
          </p>
          {idVerified && (
            <div className="mt-4 p-3 bg-green-100 border border-green-300 rounded-lg">
              <p className="text-green-800 font-medium">✅ 신분증 확인이 완료되었습니다</p>
            </div>
          )}
        </div>
      ) : (
        <div className="bg-green-50 border-2 border-green-200 rounded-lg p-6">
          <h4 className="text-lg font-bold text-gray-900 mb-4">신분증 확인</h4>
          <p className="text-gray-600 mb-4">
            고객의 신분증을 화면으로 확인한 후, 본인 확인이 완료되면 아래 버튼을 클릭하세요.
          </p>
          <div className="flex items-center gap-3">
            <input
              type="checkbox"
              id="idVerified"
              checked={idVerified}
              onChange={(e) => {
                const checked = e.target.checked;
                setIdVerified(checked);

                if (consultationInfo.id) {
                  WebSocketService.sendConsultationStepSync({
                    type: 'CONSULTATION_STEP_SYNC',
                    roomId: consultationInfo.id,
                    senderId: currentUserId,
                    receiverId: targetUserId,
                    data: { step: currentStep, idVerified: checked }
                  });
                }
              }}
              className="w-5 h-5 text-green-600 rounded focus:ring-green-500"
            />
            <label htmlFor="idVerified" className="text-gray-900 font-medium">
              신분증 확인 완료
            </label>
          </div>
          {idVerified && (
            <div className="mt-4 p-3 bg-green-100 border border-green-300 rounded-lg">
              <p className="text-green-800 font-medium">✅ 확인 완료 - 다음 단계로 진행 가능합니다</p>
            </div>
          )}
        </div>
      )}
    </div>
  );

  const openPdfDocument = (docType: string) => {
    if (!productInfo) return;

    let pdfPath = '';

    if (productType === 'deposit') {
      const fileName = `${productInfo.depositCode}_${docType}.pdf`;
      pdfPath = `/pdf/deposit/${fileName}`;
    } else if (productType === 'fund') {
      const fileName = `${productInfo.childFundCd}_${docType}.pdf`;
      pdfPath = `/pdf/fund/${fileName}`;
    }

    window.open(pdfPath, '_blank');
  };

  const downloadPdfDocument = (docType: string, docName: string) => {
    if (!productInfo) return;

    let pdfPath = '';
    let fileName = '';

    if (productType === 'deposit') {
      fileName = `${productInfo.name}_${docName}.pdf`;
      pdfPath = `/pdf/deposit/${productInfo.depositCode}_${docType}.pdf`;
    } else if (productType === 'fund') {
      fileName = `${productInfo.fundName}_${docName}.pdf`;
      pdfPath = `/pdf/fund/${productInfo.childFundCd}_${docType}.pdf`;
    }

    const link = document.createElement('a');
    link.href = pdfPath;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const renderStep2 = () => (
    <div className="space-y-6">
      <h3 className="text-xl font-bold text-gray-900">2단계: 상품 정보 확인</h3>

      {productInfo ? (
        <>
        <div className="bg-green-50 border-2 border-green-200 rounded-lg p-6">
          <div className="flex items-start gap-4 mb-4">
            <div className="text-5xl">{productType === 'deposit' ? '🏦' : '📊'}</div>
            <div className="flex-1">
              <h4 className="text-2xl font-bold text-gray-900 mb-2">
                {productInfo.name || productInfo.fundName}
              </h4>
              <p className="text-sm text-gray-600 mb-4">
                {productType === 'deposit' ? '정기예금 상품' : '펀드 상품'}
              </p>
            </div>
          </div>

          {productType === 'deposit' && (
            <div className="space-y-3">
              {}
              <div className="grid grid-cols-2 gap-4">
                <div className="bg-white p-4 rounded-lg">
                  <p className="text-sm text-gray-600 mb-1">상품 코드</p>
                  <p className="text-lg font-bold text-gray-900">{productInfo.depositCode}</p>
                </div>
                <div className="bg-white p-4 rounded-lg">
                  <p className="text-sm text-gray-600 mb-1">은행</p>
                  <p className="text-lg font-bold text-gray-900">{productInfo.bankName}</p>
                </div>
              </div>
              <div className="bg-white p-4 rounded-lg">
                <p className="text-sm text-gray-600 mb-1">상품명</p>
                <p className="text-lg font-bold text-gray-900">{productInfo.name}</p>
              </div>
              {productInfo.description && (
                <div className="bg-white p-4 rounded-lg">
                  <p className="text-sm text-gray-600 mb-1">상품 설명</p>
                  <p className="text-gray-800">{productInfo.description}</p>
                </div>
              )}
              {}
              {interestRates.length > 0 && (
                <div className="bg-white p-4 rounded-lg border border-green-200">
                  <p className="text-sm text-gray-600 mb-3">현재 적용 금리 (기준일: {new Date().toLocaleDateString()})</p>

                  {}
                  <div className="mb-4">
                    <h5 className="text-sm font-medium text-gray-700 mb-2">기본 금리</h5>
                    <div className="grid grid-cols-2 gap-3">
                      {interestRates
                        .filter((rate: any) => rate.bankCode === 'HANA' && rate.interestType === 'BASIC')
                        .slice(0, 4)
                        .map((rate: any, index: number) => (
                          <div key={index} className="bg-green-50 p-3 rounded-lg border border-green-200">
                            <p className="text-xs text-gray-600 mb-1">
                              {rate.maturityPeriod}
                            </p>
                            <p className="text-lg font-bold text-green-600">
                              {(rate.interestRate * 100).toFixed(2)}%
                            </p>
                          </div>
                        ))}
                    </div>
                  </div>

                  {}
                  {interestRates.filter((rate: any) => rate.bankCode === 'HANA' && rate.interestType === 'PREFERENTIAL').length > 0 && (
                    <div>
                      <h5 className="text-sm font-medium text-gray-700 mb-2">우대 금리</h5>
                      <div className="grid grid-cols-2 gap-3">
                        {interestRates
                          .filter((rate: any) => rate.bankCode === 'HANA' && rate.interestType === 'PREFERENTIAL')
                          .slice(0, 4)
                          .map((rate: any, index: number) => (
                            <div key={index} className="bg-blue-50 p-3 rounded-lg border border-blue-200">
                              <p className="text-xs text-gray-600 mb-1">
                                {rate.maturityPeriod}
                              </p>
                              <p className="text-lg font-bold text-blue-600">
                                {(rate.interestRate * 100).toFixed(2)}%
                              </p>
                            </div>
                          ))}
                      </div>
                    </div>
                  )}

                  {interestRates.filter((rate: any) => rate.bankCode === 'HANA').length === 0 && (
                    <p className="text-sm text-gray-500 text-center py-4">하나은행의 금리 정보가 없습니다.</p>
                  )}
                </div>
              )}
            </div>
          )}

          {productType === 'fund' && (
            <div className="space-y-3">
              <div className="grid grid-cols-2 gap-4">
                <div className="bg-white p-4 rounded-lg">
                  <p className="text-sm text-gray-600 mb-1">펀드 코드</p>
                  <p className="text-lg font-bold text-gray-900">{productInfo.fundCd}</p>
                </div>
                <div className="bg-white p-4 rounded-lg">
                  <p className="text-sm text-gray-600 mb-1">클래스 코드</p>
                  <p className="text-lg font-bold text-gray-900">{productInfo.classCode || 'N/A'}</p>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="bg-white p-4 rounded-lg">
                  <p className="text-sm text-gray-600 mb-1">자산 유형</p>
                  <p className="text-lg font-bold text-gray-900">{productInfo.assetType || '미분류'}</p>
                </div>
                <div className="bg-white p-4 rounded-lg">
                  <p className="text-sm text-gray-600 mb-1">위험 등급</p>
                  <p className="text-lg font-bold text-gray-900">{productInfo.riskGrade || '미분류'}</p>
                </div>
              </div>
              <div className="bg-white p-4 rounded-lg">
                <p className="text-sm text-gray-600 mb-1">펀드 유형</p>
                <p className="text-lg font-bold text-gray-900">
                  {productInfo.fundGb === 1 ? '공모' : productInfo.fundGb === 2 ? '사모' : '미분류'}
                </p>
              </div>
              {productInfo.sourceUrl && (
                <div className="bg-white p-4 rounded-lg">
                  <p className="text-sm text-gray-600 mb-1">상세 정보</p>
                  <a
                    href={productInfo.sourceUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-blue-600 hover:text-blue-800 underline"
                  >
                    펀드 상세페이지 보기
                  </a>
                </div>
              )}
              {}
              <div className="bg-orange-50 p-4 rounded-lg border border-orange-200">
                <p className="text-sm text-orange-800">
                  📊 펀드 투자는 원금손실 위험이 있으며, 과거 수익률이 미래 수익률을 보장하지 않습니다.
                </p>
              </div>
            </div>
          )}
        </div>

          {}
          <div className="bg-white border-2 border-purple-200 rounded-lg p-6">
            <div className="flex items-center gap-3 mb-4">
              <span className="text-3xl">📄</span>
              <h4 className="text-xl font-bold text-gray-900">상품 관련 문서</h4>
            </div>

            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-4">
              <p className="text-sm text-blue-800">
                📌 상품 가입 전 아래 문서를 반드시 확인해주세요. 각 문서를 열람하거나 다운로드할 수 있습니다.
              </p>
            </div>

            {productType === 'deposit' && (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {}
                <div className="bg-gradient-to-br from-green-50 to-green-100 border border-green-200 rounded-lg p-4">
                  <div className="flex items-start gap-3 mb-3">
                    <div className="flex-1">
                      <h5 className="font-bold text-gray-900 mb-1">상품 이용 약관</h5>
                      <p className="text-xs text-gray-600">정기예금 상품의 이용 약관 및 거래 조건</p>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <button
                      onClick={() => openPdfDocument('term')}
                      className="flex-1 px-3 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors text-sm font-medium"
                    >
                      열람
                    </button>
                    <button
                      onClick={() => downloadPdfDocument('term', '이용약관')}
                      className="flex-1 px-3 py-2 bg-white border border-green-600 text-green-600 rounded-lg hover:bg-green-50 transition-colors text-sm font-medium"
                    >
                      다운로드
                    </button>
                  </div>
                </div>

                {}
                <div className="bg-gradient-to-br from-blue-50 to-blue-100 border border-blue-200 rounded-lg p-4">
                  <div className="flex items-start gap-3 mb-3">
                    <div className="flex-1">
                      <h5 className="font-bold text-gray-900 mb-1">상품 설명서</h5>
                      <p className="text-xs text-gray-600">상품의 특징과 주요 정보 안내</p>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <button
                      onClick={() => openPdfDocument('info')}
                      className="flex-1 px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium"
                    >
                      열람
                    </button>
                    <button
                      onClick={() => downloadPdfDocument('info', '상품설명서')}
                      className="flex-1 px-3 py-2 bg-white border border-blue-600 text-blue-600 rounded-lg hover:bg-blue-50 transition-colors text-sm font-medium"
                    >
                      다운로드
                    </button>
                  </div>
                </div>
              </div>
            )}

            {productType === 'fund' && (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {}
                <div className="bg-gradient-to-br from-purple-50 to-purple-100 border border-purple-200 rounded-lg p-4">
                  <div className="flex items-start gap-3 mb-3">
                    <span className="text-2xl">📋</span>
                    <div className="flex-1">
                      <h5 className="font-bold text-gray-900 mb-1">특약</h5>
                      <p className="text-xs text-gray-600">펀드 투자 관련 특별 약정 사항</p>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <button
                      onClick={() => openPdfDocument('term')}
                      className="flex-1 px-3 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors text-sm font-medium"
                    >
                      📖 열람
                    </button>
                    <button
                      onClick={() => downloadPdfDocument('term', '특약')}
                      className="flex-1 px-3 py-2 bg-white border border-purple-600 text-purple-600 rounded-lg hover:bg-purple-50 transition-colors text-sm font-medium"
                    >
                      💾 다운로드
                    </button>
                  </div>
                </div>

                {}
                <div className="bg-gradient-to-br from-blue-50 to-blue-100 border border-blue-200 rounded-lg p-4">
                  <div className="flex items-start gap-3 mb-3">
                    <span className="text-2xl">📘</span>
                    <div className="flex-1">
                      <h5 className="font-bold text-gray-900 mb-1">투자설명서</h5>
                      <p className="text-xs text-gray-600">펀드의 상세한 투자 정보</p>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <button
                      onClick={() => openPdfDocument('info')}
                      className="flex-1 px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium"
                    >
                      📖 열람
                    </button>
                    <button
                      onClick={() => downloadPdfDocument('info', '투자설명서')}
                      className="flex-1 px-3 py-2 bg-white border border-blue-600 text-blue-600 rounded-lg hover:bg-blue-50 transition-colors text-sm font-medium"
                    >
                      💾 다운로드
                    </button>
                  </div>
                </div>

                {}
                <div className="bg-gradient-to-br from-green-50 to-green-100 border border-green-200 rounded-lg p-4">
                  <div className="flex items-start gap-3 mb-3">
                    <span className="text-2xl">📗</span>
                    <div className="flex-1">
                      <h5 className="font-bold text-gray-900 mb-1">간이투자설명서</h5>
                      <p className="text-xs text-gray-600">핵심 투자 정보 요약</p>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <button
                      onClick={() => openPdfDocument('info_simple')}
                      className="flex-1 px-3 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors text-sm font-medium"
                    >
                      📖 열람
                    </button>
                    <button
                      onClick={() => downloadPdfDocument('info_simple', '간이투자설명서')}
                      className="flex-1 px-3 py-2 bg-white border border-green-600 text-green-600 rounded-lg hover:bg-green-50 transition-colors text-sm font-medium"
                    >
                      💾 다운로드
                    </button>
                  </div>
                </div>

                {}
                <div className="bg-gradient-to-br from-orange-50 to-orange-100 border border-orange-200 rounded-lg p-4">
                  <div className="flex items-start gap-3 mb-3">
                    <span className="text-2xl">📊</span>
                    <div className="flex-1">
                      <h5 className="font-bold text-gray-900 mb-1">운용보고서</h5>
                      <p className="text-xs text-gray-600">펀드 운용 현황 및 성과</p>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <button
                      onClick={() => openPdfDocument('report')}
                      className="flex-1 px-3 py-2 bg-orange-600 text-white rounded-lg hover:bg-orange-700 transition-colors text-sm font-medium"
                    >
                      📖 열람
                    </button>
                    <button
                      onClick={() => downloadPdfDocument('report', '운용보고서')}
                      className="flex-1 px-3 py-2 bg-white border border-orange-600 text-orange-600 rounded-lg hover:bg-orange-50 transition-colors text-sm font-medium"
                    >
                      💾 다운로드
                    </button>
                  </div>
                </div>
              </div>
            )}

            <div className="mt-4 bg-red-50 border border-red-200 rounded-lg p-3">
              <p className="text-sm text-red-800">
                ⚠️ 상품 가입 전 관련 문서를 반드시 숙지하시기 바랍니다. 문서의 내용을 충분히 이해한 후 가입을 진행해주세요.
              </p>
            </div>
          </div>
        </>
      ) : (
        <div className="bg-gray-50 border border-gray-200 rounded-lg p-6 text-center">
          <p className="text-gray-600">상품 정보를 불러오는 중...</p>
        </div>
      )}

      <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
        <h5 className="font-bold text-yellow-900 mb-2">💡 상담사 안내</h5>
        <p className="text-sm text-yellow-800">
          상담사가 상품에 대해 자세히 설명드립니다. 궁금한 사항이 있으시면 언제든지 질문해주세요.
        </p>
        {productType === 'deposit' && (
          <p className="text-sm text-yellow-800 mt-2">
            💰 정기예금의 경우 실제 가입 시점의 금리가 적용되며, 금리는 시장 상황에 따라 변동될 수 있습니다.
          </p>
        )}
        {productType === 'fund' && (
          <p className="text-sm text-yellow-800 mt-2">
            📈 펀드 투자는 원금손실 위험이 있으며, 과거 수익률이 미래 수익률을 보장하지 않습니다.
          </p>
        )}
      </div>
    </div>
  );

  const renderStep3 = () => (
    <div className="space-y-6">
      <h3 className="text-xl font-bold text-gray-900">3단계: 가입 정보 입력 및 예상 수익 확인</h3>

      {}
      <div className="bg-gradient-to-r from-green-50 to-blue-50 border-2 border-green-200 rounded-xl p-6">
        <div className="flex items-center gap-3 mb-4">
          <div className="w-12 h-12 bg-green-600 rounded-full flex items-center justify-center">
            <span className="text-2xl">💳</span>
          </div>
          <div>
            <h4 className="text-lg font-bold text-gray-900">고객 IRP 계좌</h4>
            <p className="text-sm text-gray-600">가입 시 사용될 계좌입니다</p>
          </div>
        </div>

        {irpAccount ? (
          <div className="bg-white rounded-lg p-4 space-y-3">
            <div className="flex justify-between items-center pb-2 border-b border-gray-200">
              <span className="text-sm text-gray-600">은행</span>
              <span className="font-bold text-gray-900">{irpAccount.bankName}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-600">상품명</span>
              <span className="font-medium text-gray-900">{irpAccount.productName}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-600">계좌번호</span>
              <span className="font-mono font-bold text-gray-900">{irpAccount.displayAccountNumber}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-600">현재 잔액</span>
              <span className="font-bold text-green-600 text-lg">
                {irpAccount.currentBalance.toLocaleString()}원
              </span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-600">누적 납입금</span>
              <span className="font-medium text-blue-600">
                {irpAccount.totalContribution.toLocaleString()}원
              </span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-600">투자 성향</span>
              <span className="font-medium text-purple-600">
                {irpAccount.investmentStyleDisplay || irpAccount.investmentStyle}
              </span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-600">개설일</span>
              <span className="font-medium text-gray-700">
                {new Date(irpAccount.openDate).toLocaleDateString()}
              </span>
            </div>
            <div className="flex justify-between items-center pt-2 border-t border-gray-200">
              <span className="text-sm text-gray-600">계좌 상태</span>
              <span className={`font-medium px-2 py-1 rounded text-sm ${
                irpAccount.accountStatus === 'ACTIVE'
                  ? 'bg-green-100 text-green-800'
                  : 'bg-gray-100 text-gray-800'
              }`}>
                {irpAccount.accountStatus === 'ACTIVE' ? '정상' : irpAccount.accountStatus}
              </span>
            </div>
          </div>
        ) : (
          <div className="bg-white rounded-lg p-4 text-center">
            <p className="text-gray-500">IRP 계좌 정보를 불러오는 중...</p>
          </div>
        )}
      </div>

      {}
      {productType === 'deposit' && (
        <div className="space-y-6">
          {}
          <div className="bg-blue-50 border-2 border-blue-200 rounded-xl p-6">
            <h4 className="text-lg font-bold text-gray-900 mb-4 flex items-center">
              <span className="text-2xl mr-2">👔</span>
              상담사 입력 영역
            </h4>

        <div className="space-y-4">
              <div className="bg-blue-100 border border-blue-300 rounded-lg p-3 mb-4">
                <p className="text-sm text-blue-800">
                  💡 IRP 계좌 내에서 예금 상품에 가입하는 것이므로 별도의 계좌가 생성되지 않습니다.
                </p>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              가입 금액 (원) <span className="text-red-500">*</span>
            </label>
            <input
              type="number"
              value={depositFormData.initialBalance}
              onChange={(e) => setDepositFormData(prev => ({ ...prev, initialBalance: e.target.value }))}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder="최소 1,000,000원"
              min="1000000"
                  disabled={currentUserRole === 'customer'}
            />
            {depositFormData.initialBalance && Number(depositFormData.initialBalance) < 1000000 && (
              <p className="text-sm text-red-600 mt-1">최소 가입 금액은 1,000,000원입니다</p>
            )}
                {irpAccount && depositFormData.initialBalance && Number(depositFormData.initialBalance) > irpAccount.currentBalance && (
                  <p className="text-sm text-orange-600 mt-1">⚠️ IRP 계좌 잔액보다 큽니다</p>
                )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              가입 기간 <span className="text-red-500">*</span>
            </label>
            <select
              value={depositFormData.depositPeriod}
              onChange={(e) => setDepositFormData(prev => ({ ...prev, depositPeriod: Number(e.target.value) }))}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  disabled={currentUserRole === 'customer'}
            >
              {[6, 12, 18, 24, 30, 36].map(month => (
                <option key={month} value={month}>{month}개월</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              이자 지급 방법 <span className="text-red-500">*</span>
            </label>
            <div className="flex gap-4">
              <label className="flex items-center">
                <input
                  type="radio"
                  value="AUTO"
                  checked={depositFormData.interestPaymentMethod === 'AUTO'}
                  onChange={(e) => setDepositFormData(prev => ({ ...prev, interestPaymentMethod: e.target.value as 'AUTO' | 'MANUAL' }))}
                  className="mr-2"
                      disabled={currentUserRole === 'customer'}
                />
                자동 이체
              </label>
              <label className="flex items-center">
                <input
                  type="radio"
                  value="MANUAL"
                  checked={depositFormData.interestPaymentMethod === 'MANUAL'}
                  onChange={(e) => setDepositFormData(prev => ({ ...prev, interestPaymentMethod: e.target.value as 'AUTO' | 'MANUAL' }))}
                  className="mr-2"
                      disabled={currentUserRole === 'customer'}
                />
                수동 지급
              </label>
                </div>
              </div>
            </div>
          </div>

          {}
          {depositProjection && (
            <div className="bg-gradient-to-br from-green-50 to-emerald-50 border-2 border-green-300 rounded-xl p-6">
              <h4 className="text-lg font-bold text-gray-900 mb-4 flex items-center">
                <span className="text-2xl mr-2">💰</span>
                예상 수익 분석
              </h4>

              <div className="grid grid-cols-3 gap-4 mb-6">
                <div className="bg-white rounded-lg p-4 text-center">
                  <p className="text-sm text-gray-600 mb-1">원금</p>
                  <p className="text-xl font-bold text-gray-900">
                    {depositProjection.principal.toLocaleString()}원
                  </p>
                </div>
                <div className="bg-white rounded-lg p-4 text-center">
                  <p className="text-sm text-gray-600 mb-1">예상 이자</p>
                  <p className="text-xl font-bold text-green-600">
                    +{depositProjection.interest.toLocaleString()}원
                  </p>
                </div>
                <div className="bg-gradient-to-r from-green-500 to-emerald-500 rounded-lg p-4 text-center">
                  <p className="text-sm text-white mb-1">만기 수령액</p>
                  <p className="text-xl font-bold text-white">
                    {depositProjection.total.toLocaleString()}원
                  </p>
                </div>
              </div>

              {}
              <div className="bg-white rounded-lg p-4">
                <p className="text-sm font-medium text-gray-700 mb-3">월별 예상 수익 추이</p>
                <div className="space-y-2 max-h-48 overflow-y-auto">
                  {depositProjection.monthlyBreakdown
                    .filter((_, idx) => idx % Math.ceil(depositProjection.monthlyBreakdown.length / 6) === 0 || idx === depositProjection.monthlyBreakdown.length - 1)
                    .map((item) => (
                      <div key={item.month} className="flex items-center gap-2">
                        <span className="text-xs text-gray-600 w-16">{item.month}개월</span>
                        <div className="flex-1 bg-gray-100 rounded-full h-6 relative overflow-hidden">
                          <div
                            className="bg-gradient-to-r from-green-400 to-green-600 h-full rounded-full flex items-center justify-end pr-2"
                            style={{ width: `${(item.total / depositProjection.total) * 100}%` }}
                          >
                            <span className="text-xs font-bold text-white">
                              {item.total.toLocaleString()}원
                            </span>
                          </div>
                        </div>
                      </div>
                    ))}
                </div>
              </div>

              <div className="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
                <p className="text-xs text-blue-800 font-medium mb-1">
                  📊 적용 금리 정보
                </p>
                <p className="text-xs text-blue-700">
                  • 가입 기간: {depositFormData.depositPeriod}개월<br />
                  • 적용 금리: 연 {(findInterestRateForPeriod(depositFormData.depositPeriod, productInfo?.bankCode || 'HANA') * 100).toFixed(2)}% (기본 금리)<br />
                  • 계산 방식: 복리
                </p>
              </div>
              <div className="mt-2 p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
                <p className="text-xs text-yellow-800">
                  💡 위 수치는 현재 기준 금리로 계산된 예상 금액입니다. 실제 금리는 가입 시점에 확정되며 우대 조건 적용 시 추가 이율이 발생할 수 있습니다.
                </p>
              </div>
            </div>
          )}

          {}
          <div className="bg-purple-50 border-2 border-purple-200 rounded-xl p-6">
            <h4 className="text-lg font-bold text-gray-900 mb-4 flex items-center">
              <span className="text-2xl mr-2">🔐</span>
              고객 본인 인증 (IRP 계좌 비밀번호)
            </h4>

            {currentUserRole === 'customer' ? (
              <div className="space-y-4">
                <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-3 mb-4">
                  <p className="text-sm text-yellow-800">
                    💡 기존 IRP 계좌의 4자리 비밀번호를 입력해주세요.
                  </p>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
                    IRP 계좌 비밀번호 (4자리 숫자) <span className="text-red-500">*</span>
            </label>
            <input
              type="password"
                    inputMode="numeric"
                    pattern="[0-9]*"
                    maxLength={4}
                    value={irpPassword.password}
                    onChange={(e) => {
                      const value = e.target.value.replace(/[^0-9]/g, '');
                      setIrpPassword(prev => ({ ...prev, password: value }));
                    }}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 text-center text-2xl tracking-widest"
                    placeholder="••••"
                  />
                  {irpPassword.password && irpPassword.password.length !== 4 && (
                    <p className="text-sm text-orange-600 mt-1">⚠️ 4자리 숫자를 입력해주세요</p>
                  )}
                  {irpPassword.password.length === 4 && (
                    <p className="text-sm text-green-600 mt-1">✅ 비밀번호 입력 완료</p>
                  )}
                </div>
              </div>
            ) : (
              <div className="bg-white rounded-lg p-6 text-center">
                <div className="text-4xl mb-3">🔒</div>
                <p className="text-gray-700 font-medium mb-2">고객이 IRP 계좌 비밀번호를 입력 중입니다</p>
                <p className="text-sm text-gray-500">보안을 위해 상담사는 비밀번호를 확인할 수 없습니다</p>
                {passwordCompleted && (
                  <div className="mt-4 p-3 bg-green-100 border border-green-300 rounded-lg">
                    <p className="text-green-800 font-medium">✅ 고객이 비밀번호 입력을 완료했습니다</p>
                  </div>
                )}
                {!passwordCompleted && (
                  <div className="mt-4 p-3 bg-gray-100 border border-gray-300 rounded-lg">
                    <p className="text-gray-600">⏳ 고객의 비밀번호 입력을 기다리고 있습니다...</p>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      )}

      {}
      {productType === 'fund' && (
        <div className="space-y-6">
          {}
          <div className="bg-blue-50 border-2 border-blue-200 rounded-xl p-6">
            <h4 className="text-lg font-bold text-gray-900 mb-4 flex items-center">
              <span className="text-2xl mr-2">👔</span>
              상담사 입력 영역
            </h4>

        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              거래 유형 <span className="text-red-500">*</span>
            </label>
            <div className="flex gap-4">
              <label className="flex items-center">
                <input
                  type="radio"
                  value="buy"
                  checked={fundFormData.transactionType === 'buy'}
                  onChange={(e) => setFundFormData(prev => ({ ...prev, transactionType: e.target.value as TransactionType }))}
                  className="mr-2"
                      disabled={currentUserRole === 'customer'}
                />
                매수
              </label>
              <label className="flex items-center">
                <input
                  type="radio"
                  value="sell"
                  checked={fundFormData.transactionType === 'sell'}
                  onChange={(e) => setFundFormData(prev => ({ ...prev, transactionType: e.target.value as TransactionType }))}
                  className="mr-2"
                      disabled={currentUserRole === 'customer'}
                />
                매도
              </label>
            </div>
          </div>

          {fundFormData.transactionType === 'buy' ? (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                매수 금액 (원) <span className="text-red-500">*</span>
              </label>
              <input
                type="number"
                value={fundFormData.amount}
                onChange={(e) => setFundFormData(prev => ({ ...prev, amount: e.target.value }))}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="매수할 금액 입력"
                min="1"
                    disabled={currentUserRole === 'customer'}
              />
                  {irpAccount && fundFormData.amount && Number(fundFormData.amount) > irpAccount.currentBalance && (
                    <p className="text-sm text-orange-600 mt-1">⚠️ IRP 계좌 잔액보다 큽니다</p>
                  )}
            </div>
          ) : (
            <>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  매도할 펀드 선택 <span className="text-red-500">*</span>
                </label>
                {userFundPortfolio.length > 0 ? (
                  <div className="space-y-2">
                    {userFundPortfolio.map((portfolio) => (
                      <div
                        key={portfolio.subscriptionId}
                        className={`border-2 rounded-lg p-4 cursor-pointer transition-colors ${
                          fundFormData.selectedSubscription?.subscriptionId === portfolio.subscriptionId
                                ? 'border-blue-500 bg-blue-50'
                                : 'border-gray-200 hover:border-blue-300'
                            } ${currentUserRole === 'customer' ? 'pointer-events-none opacity-60' : ''}`}
                            onClick={() => currentUserRole === 'counselor' && setFundFormData(prev => ({ ...prev, selectedSubscription: portfolio }))}
                      >
                        <p className="font-bold text-gray-900">{portfolio.fundName}</p>
                        <p className="text-sm text-gray-600">보유 좌수: {portfolio.totalUnits.toFixed(4)}</p>
                        <p className="text-sm text-gray-600">평가액: {portfolio.currentValue.toLocaleString()}원</p>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-gray-600 text-center py-4">보유 중인 펀드가 없습니다</p>
                )}
              </div>

              {fundFormData.selectedSubscription && (
                <>
                  <div>
                    <label className="flex items-center mb-2">
                      <input
                        type="checkbox"
                        checked={fundFormData.sellAll}
                        onChange={(e) => setFundFormData(prev => ({ ...prev, sellAll: e.target.checked }))}
                        className="mr-2"
                            disabled={currentUserRole === 'customer'}
                      />
                      전량 매도
                    </label>
                  </div>

                  {!fundFormData.sellAll && (
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        매도 좌수 <span className="text-red-500">*</span>
                      </label>
                      <input
                        type="number"
                        value={fundFormData.sellUnits}
                        onChange={(e) => setFundFormData(prev => ({ ...prev, sellUnits: e.target.value }))}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                        placeholder="매도할 좌수 입력"
                        max={fundFormData.selectedSubscription.totalUnits}
                        step="0.0001"
                            disabled={currentUserRole === 'customer'}
                      />
                      <p className="text-sm text-gray-600 mt-1">
                        보유 좌수: {fundFormData.selectedSubscription.totalUnits.toFixed(4)}
                      </p>
                    </div>
                  )}
                </>
              )}
            </>
          )}
            </div>
          </div>

          {}
          {fundFormData.transactionType === 'buy' && fundProjection && (
            <div className="bg-gradient-to-br from-purple-50 to-pink-50 border-2 border-purple-300 rounded-xl p-6">
              <h4 className="text-lg font-bold text-gray-900 mb-4 flex items-center">
                <span className="text-2xl mr-2">📈</span>
                펀드 예상 수익 시나리오
              </h4>

              <div className="bg-orange-50 border border-orange-200 rounded-lg p-3 mb-4">
                <p className="text-xs text-orange-800">
                  ⚠️ 펀드 투자는 원금 손실 위험이 있으며, 아래는 가상의 시나리오입니다.
                </p>
              </div>

              <div className="space-y-3">
                {fundProjection.scenarios.map((scenario, idx) => (
                  <div key={idx} className="bg-white rounded-lg p-4 border-2 border-gray-200 hover:border-purple-300 transition-colors">
                    <div className="flex justify-between items-center mb-2">
                      <span className="font-bold text-gray-900">{scenario.label}</span>
                      <span className={`text-lg font-bold ${scenario.profit >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                        {scenario.profit >= 0 ? '+' : ''}{scenario.profit.toLocaleString()}원
                      </span>
                    </div>
                    <div className="flex items-center gap-2">
                      <div className="flex-1 bg-gray-100 rounded-full h-8 relative overflow-hidden">
                        <div
                          className={`h-full rounded-full flex items-center justify-center ${
                            scenario.profit >= 0
                              ? 'bg-gradient-to-r from-green-400 to-green-600'
                              : 'bg-gradient-to-r from-red-400 to-red-600'
                          }`}
                          style={{
                            width: `${Math.max(10, Math.min(100, 50 + (scenario.rate * 100)))}%`
                          }}
                        >
                          <span className="text-sm font-bold text-white">
                            {(scenario.rate * 100).toFixed(0)}%
                          </span>
                        </div>
                      </div>
                      <span className="font-bold text-gray-900 w-32 text-right">
                        {scenario.finalAmount.toLocaleString()}원
                      </span>
                    </div>
                  </div>
                ))}
              </div>

              <div className="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
                <p className="text-xs text-blue-800">
                  💡 실제 수익률은 시장 상황에 따라 변동되며, 과거 수익률이 미래 수익을 보장하지 않습니다.
                </p>
              </div>
            </div>
          )}

          {}
          <div className="bg-purple-50 border-2 border-purple-200 rounded-xl p-6">
            <h4 className="text-lg font-bold text-gray-900 mb-4 flex items-center">
              <span className="text-2xl mr-2">🔐</span>
              고객 본인 인증 (IRP 계좌 비밀번호)
            </h4>

            {currentUserRole === 'customer' ? (
              <div className="space-y-4">
                <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-3 mb-4">
                  <p className="text-sm text-yellow-800">
                    💡 기존 IRP 계좌의 4자리 비밀번호를 입력해주세요.
                  </p>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    IRP 계좌 비밀번호 (4자리 숫자) <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="password"
                    inputMode="numeric"
                    pattern="[0-9]*"
                    maxLength={4}
                    value={irpPassword.password}
                    onChange={(e) => {
                      const value = e.target.value.replace(/[^0-9]/g, '');
                      setIrpPassword(prev => ({ ...prev, password: value }));
                    }}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 text-center text-2xl tracking-widest"
                    placeholder="••••"
                  />
                  {irpPassword.password && irpPassword.password.length !== 4 && (
                    <p className="text-sm text-orange-600 mt-1">⚠️ 4자리 숫자를 입력해주세요</p>
                  )}
                </div>

              </div>
            ) : (
              <div className="bg-white rounded-lg p-6 text-center">
                <div className="text-4xl mb-3">🔒</div>
                <p className="text-gray-700 font-medium mb-2">고객이 IRP 계좌 비밀번호를 입력 중입니다</p>
                <p className="text-sm text-gray-500">보안을 위해 상담사는 비밀번호를 확인할 수 없습니다</p>
                {passwordCompleted && (
                  <div className="mt-4 p-3 bg-green-100 border border-green-300 rounded-lg">
                    <p className="text-green-800 font-medium">✅ 고객이 비밀번호 입력을 완료했습니다</p>
                  </div>
                )}
                {!passwordCompleted && (
                  <div className="mt-4 p-3 bg-gray-100 border border-gray-300 rounded-lg">
                    <p className="text-gray-600">⏳ 고객의 비밀번호 입력을 기다리고 있습니다...</p>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );

  const renderStep4 = () => (
    <div className="space-y-6">
      <h3 className="text-xl font-bold text-gray-900">4단계: 최종 확인 및 승인</h3>

      {!processResult ? (
        <>
          <div className="bg-blue-50 border-2 border-blue-200 rounded-lg p-6">
            <h4 className="text-lg font-bold text-gray-900 mb-4">가입 정보 확인</h4>

            <div className="space-y-3">
              <div className="flex justify-between py-2 border-b border-blue-200">
                <span className="text-gray-600">상품명</span>
                <span className="font-medium text-gray-900">
                  {productInfo?.name || productInfo?.fundName}
                </span>
              </div>

              <div className="flex justify-between py-2 border-b border-blue-200">
                <span className="text-gray-600">상품 유형</span>
                <span className="font-medium text-gray-900">
                  {productType === 'deposit' ? '정기예금' : '펀드'}
                </span>
              </div>

              {productType === 'deposit' && (
                <>
                  <div className="flex justify-between py-2 border-b border-blue-200">
                    <span className="text-gray-600">투자 금액</span>
                    <span className="font-medium text-gray-900">
                      {Number(depositFormData.initialBalance).toLocaleString()}원
                    </span>
                  </div>
                  <div className="flex justify-between py-2 border-b border-blue-200">
                    <span className="text-gray-600">가입 기간</span>
                    <span className="font-medium text-gray-900">{depositFormData.depositPeriod}개월</span>
                  </div>
                  <div className="flex justify-between py-2 border-b border-blue-200">
                    <span className="text-gray-600">이자 지급 방법</span>
                    <span className="font-medium text-gray-900">
                      {depositFormData.interestPaymentMethod === 'AUTO' ? '자동 이체' : '수동 지급'}
                    </span>
                  </div>
                  <div className="flex justify-between py-2 border-b border-blue-200">
                    <span className="text-gray-600">관리 계좌</span>
                    <span className="font-medium text-gray-900">
                      IRP 계좌 ({irpAccount?.displayAccountNumber})
                    </span>
                  </div>
                </>
              )}

              {productType === 'fund' && (
                <>
                  <div className="flex justify-between py-2 border-b border-blue-200">
                    <span className="text-gray-600">거래 유형</span>
                    <span className="font-medium text-gray-900">
                      {fundFormData.transactionType === 'buy' ? '매수' : '매도'}
                    </span>
                  </div>
                  {fundFormData.transactionType === 'buy' ? (
                    <div className="flex justify-between py-2 border-b border-blue-200">
                      <span className="text-gray-600">매수 금액</span>
                      <span className="font-medium text-gray-900">
                        {Number(fundFormData.amount).toLocaleString()}원
                      </span>
                    </div>
                  ) : (
                    <>
                      <div className="flex justify-between py-2 border-b border-blue-200">
                        <span className="text-gray-600">매도 펀드</span>
                        <span className="font-medium text-gray-900">
                          {fundFormData.selectedSubscription?.fundName}
                        </span>
                      </div>
                      <div className="flex justify-between py-2 border-b border-blue-200">
                        <span className="text-gray-600">매도 방식</span>
                        <span className="font-medium text-gray-900">
                          {fundFormData.sellAll
                            ? '전량 매도'
                            : `${Number(fundFormData.sellUnits).toFixed(4)} 좌`
                          }
                        </span>
                      </div>
                    </>
                  )}
                </>
              )}
            </div>
          </div>

          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <p className="text-sm text-blue-800">
              💡 IRP 계좌 내에서 포트폴리오로 관리됩니다. 별도의 계좌가 생성되지 않습니다.
            </p>
          </div>

          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
            <p className="text-sm text-yellow-800">
              ⚠️ 위 정보를 확인하신 후, 상담사가 최종 승인 버튼을 클릭하면 가입이 완료됩니다.
            </p>
          </div>

          {currentUserRole === 'counselor' && (
            <button
              onClick={handleFinalApproval}
              disabled={isProcessing}
              className="w-full py-3 bg-green-600 hover:bg-green-700 disabled:bg-gray-300 text-white rounded-lg font-bold text-lg transition-colors"
            >
              {isProcessing ? '처리 중...' : '가입 승인'}
            </button>
          )}
        </>
      ) : (
        <div className={`border-2 rounded-lg p-6 ${
          processResult.success
            ? 'bg-green-50 border-green-200'
            : 'bg-red-50 border-red-200'
        }`}>
          <div className="text-center">
            <div className="text-6xl mb-4">
              {processResult.success ? '✅' : '❌'}
            </div>
            <h4 className={`text-2xl font-bold mb-2 ${
              processResult.success ? 'text-green-900' : 'text-red-900'
            }`}>
              {processResult.success ? '가입 완료' : '가입 실패'}
            </h4>
            <p className={`text-lg ${
              processResult.success ? 'text-green-800' : 'text-red-800'
            }`}>
              {processResult.message}
            </p>
          </div>
        </div>
      )}
    </div>
  );

  const renderProgressIndicator = () => (
    <div className="flex items-center justify-between mb-8">
      {[1, 2, 3, 4].map((step) => (
        <React.Fragment key={step}>
          <div className="flex flex-col items-center flex-1">
            <div className={`w-10 h-10 rounded-full flex items-center justify-center font-bold transition-colors ${
              currentStep === step
                ? 'bg-green-600 text-white'
                : currentStep > step
                ? 'bg-green-500 text-white'
                : 'bg-gray-200 text-gray-500'
            }`}>
              {currentStep > step ? '✓' : step}
            </div>
            <p className={`text-xs mt-2 font-medium ${
              currentStep >= step ? 'text-green-600' : 'text-gray-400'
            }`}>
              {step === 1 && '신분증 확인'}
              {step === 2 && '상품 확인'}
              {step === 3 && '정보 입력'}
              {step === 4 && '최종 승인'}
            </p>
          </div>
          {step < 4 && (
            <div className={`h-1 flex-1 mx-2 transition-colors ${
              currentStep > step ? 'bg-green-500' : 'bg-gray-200'
            }`} />
          )}
        </React.Fragment>
      ))}
    </div>
  );

  return (
    <div className="bg-white rounded-xl shadow-lg p-6 h-full flex flex-col overflow-y-auto scrollbar-thin scrollbar-thumb-gray-300 scrollbar-track-gray-100 hover:scrollbar-thumb-gray-400">
      <div className="flex items-center gap-3 mb-6">
        <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center">
          <span className="text-2xl">📋</span>
        </div>
        <div>
          <h2 className="text-2xl font-bold text-gray-900">상품 가입 상담</h2>
          <p className="text-sm text-gray-600">
            {productType === 'deposit' ? '정기예금 가입 절차' : '펀드 거래 절차'}
          </p>
        </div>
      </div>

      {}
      {renderProgressIndicator()}

      {}
      {error && (
        <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg">
          <p className="text-red-800">{error}</p>
        </div>
      )}

      {}
      <div className="flex-1 mb-6">
        {currentStep === 1 && renderStep1()}
        {currentStep === 2 && renderStep2()}
        {currentStep === 3 && renderStep3()}
        {currentStep === 4 && renderStep4()}
      </div>

      {}
      {currentUserRole === 'counselor' && !processResult && (
        <div className="flex gap-3 pt-4 border-t">
          {currentStep > 1 && (
            <button
              onClick={handlePrevStep}
              className="flex-1 py-2 px-4 border-2 border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-50 transition-colors"
            >
              이전 단계
            </button>
          )}
          {currentStep < 4 && (
            <button
              onClick={handleNextStep}
              disabled={!canProceedToNextStep()}
              className="flex-1 py-2 px-4 bg-green-600 hover:bg-green-700 disabled:bg-gray-300 text-white rounded-lg font-medium transition-colors"
            >
              다음 단계
            </button>
          )}
        </div>
      )}

      {}
      <div className="mt-6 pt-6 border-t">
        <h3 className="text-lg font-bold text-gray-900 mb-3 flex items-center">
          <svg className="w-5 h-5 mr-2 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
          </svg>
          상담 기록
        </h3>

        <NotesTab
          consultationId={consultationInfo.id || ''}
          currentUserId={currentUserId}
          currentUserRole={currentUserRole}
          targetUserId={targetUserId}
        />
      </div>
    </div>
  );
};

export default ProductConsultation;