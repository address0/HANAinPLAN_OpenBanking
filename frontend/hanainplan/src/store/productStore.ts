import { create } from 'zustand';

interface ProductCategory {
    name: string;
    detail: string;
    color: string;
}

interface ProductCategoryStore {
    categories: ProductCategory[];
}

const productStore = create<ProductCategoryStore>((_) => ({
    categories: [
        {
            name: '자산',
            detail: '연금/투자보험, 저축보험',
            color: "#FF0000",
        },
        {
            name: '정기',
            detail: '정기보험',
            color: '#008559',
        },
        {
            name: '종신',
            detail: '종신보험',
            color: '#0073FF',
        },
        {
            name: '의료',
            detail: '암/CI/간병보험, 질병보험, 실손의료보험',
            color: '#BB00FF',
        },
        {
            name: '기타',
            detail: '기타 보험상품',
            color: '#000000',
        }
    ]
}))

export default productStore;