import React, { useState, useEffect } from 'react';
import { getInsuranceProducts } from '../../api/insuranceApi';
import type { InsuranceProduct } from '../../types/insurance';

interface ProductSelectionProps {
  onProductSelect: (product: InsuranceProduct) => void;
  onNext: () => void;
}

const ProductSelection: React.FC<ProductSelectionProps> = ({ onProductSelect, onNext }) => {
  const [products, setProducts] = useState<InsuranceProduct[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const categories = [
    { value: '', label: '전체' },
    { value: '생명보험', label: '생명보험' },
    { value: '건강보험', label: '건강보험' },
    { value: '자동차보험', label: '자동차보험' },
    { value: '여행보험', label: '여행보험' },
    { value: '화재보험', label: '화재보험' }
  ];

  useEffect(() => {
    loadProducts();
  }, [selectedCategory]);

  const loadProducts = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const productData = await getInsuranceProducts(selectedCategory || undefined);
      setProducts(productData);
    } catch (err) {
      setError('보험 상품을 불러오는데 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleProductSelect = (product: InsuranceProduct) => {
    onProductSelect(product);
    onNext();
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW',
      minimumFractionDigits: 0
    }).format(amount);
  };

  return (
    <div className="space-y-6">
      {}
      <div className="text-center">
        <h1 className="text-2xl font-bold text-gray-800 mb-2">보험 상품 선택</h1>
        <p className="text-gray-600">고객님께 맞는 최적의 보험 상품을 선택해주세요</p>
      </div>

      {}
      <div className="bg-white rounded-lg shadow p-4">
        <h2 className="text-lg font-semibold text-gray-800 mb-3">보험 카테고리</h2>
        <div className="flex flex-wrap gap-2">
          {categories.map((category) => (
            <button
              key={category.value}
              onClick={() => setSelectedCategory(category.value)}
              className={`px-3 py-2 rounded-lg font-medium transition-colors text-sm ${
                selectedCategory === category.value
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              {category.label}
            </button>
          ))}
        </div>
      </div>

      {}
      <div className="bg-white rounded-lg shadow p-4">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">보험 상품 목록</h2>

        {isLoading ? (
          <div className="flex justify-center items-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          </div>
        ) : error ? (
          <div className="text-center py-8">
            <p className="text-red-600 mb-4">{error}</p>
            <button
              onClick={loadProducts}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm"
            >
              다시 시도
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {products.map((product) => (
              <div key={product.id} className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
                <div className="mb-3">
                  <div className="flex items-center justify-between mb-2">
                    <span className="px-2 py-1 bg-blue-100 text-blue-800 text-xs font-medium rounded-full">
                      {product.category}
                    </span>
                    <span className="text-xs text-gray-500">
                      {product.isActive ? '판매중' : '판매중단'}
                    </span>
                  </div>
                  <h3 className="text-lg font-bold text-gray-800 mb-1">{product.name}</h3>
                  <p className="text-gray-600 text-sm line-clamp-2">{product.description}</p>
                </div>

                <div className="mb-3">
                  <h4 className="font-semibold text-gray-800 mb-1 text-sm">보장 내용</h4>
                  <p className="text-xs text-gray-600 mb-2">{product.coverage}</p>
                  <div className="space-y-1">
                    {product.benefits.slice(0, 2).map((benefit, index) => (
                      <div key={index} className="flex items-center text-xs text-gray-600">
                        <svg className="w-3 h-3 text-green-500 mr-1" fill="currentColor" viewBox="0 0 20 20">
                          <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                        </svg>
                        {benefit}
                      </div>
                    ))}
                  </div>
                </div>

                <div className="mb-3">
                  <div className="flex justify-between text-xs text-gray-600 mb-1">
                    <span>가입 연령</span>
                    <span>{product.minAge}세 - {product.maxAge}세</span>
                  </div>
                  <div className="flex justify-between text-xs text-gray-600">
                    <span>보험료</span>
                    <span className="font-semibold text-blue-600">
                      {formatCurrency(product.minPremium)} ~ {formatCurrency(product.maxPremium)}
                    </span>
                  </div>
                </div>

                <button
                  onClick={() => handleProductSelect(product)}
                  disabled={!product.isActive}
                  className={`w-full py-2 rounded-lg font-medium transition-colors text-sm ${
                    product.isActive
                      ? 'bg-blue-600 text-white hover:bg-blue-700'
                      : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                  }`}
                >
                  {product.isActive ? '가입 신청하기' : '판매 중단'}
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default ProductSelection;