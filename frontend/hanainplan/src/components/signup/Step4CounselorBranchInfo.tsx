import { useState, useEffect } from 'react'
import KakaoMap from '../map/KakaoMap'

interface BranchInfo {
  branchCode: string;
  branchName: string;
  address: string;
  coordinates: {
    latitude: number;
    longitude: number;
  };
}

interface Step4CounselorBranchInfoProps {
  branchInfo: BranchInfo;
  onBranchInfoChange: (branchInfo: BranchInfo) => void;
}

// 카카오맵 장소 검색 결과 타입
interface PlaceSearchResult {
  id: string;
  place_name: string;
  address_name: string;
  road_address_name: string;
  x: string; // longitude
  y: string; // latitude
  category_name: string;
}

function Step4CounselorBranchInfo({ branchInfo, onBranchInfoChange }: Step4CounselorBranchInfoProps) {
  const [searchQuery, setSearchQuery] = useState('')
  const [searchResults, setSearchResults] = useState<PlaceSearchResult[]>([])
  const [showMap, setShowMap] = useState(true)
  const [errors, setErrors] = useState<Partial<BranchInfo>>({})
  const [mapMarkers, setMapMarkers] = useState<Array<{
    latitude: number;
    longitude: number;
    title: string;
    address: string;
  }>>([])
  const [isSearching, setIsSearching] = useState(false)

  // 카카오맵 장소 검색 함수
  const searchPlaces = async (query: string) => {
    if (!window.kakao || !window.kakao.maps || !window.kakao.maps.services) {
      console.error('카카오맵 API 또는 서비스가 로드되지 않았습니다.')
      return
    }

    setIsSearching(true)
    
    try {
      const ps = new window.kakao.maps.services.Places()
      
      ps.keywordSearch(query, (data: PlaceSearchResult[], status: any) => {
        setIsSearching(false)
        
        if (status === window.kakao.maps.services.Status.OK) {
          // 검색 결과를 그대로 사용 (필터링 제거)
          setSearchResults(data)
          
          // 검색 결과를 지도 마커로 표시
          setMapMarkers(data.map(place => ({
            latitude: parseFloat(place.y),
            longitude: parseFloat(place.x),
            title: place.place_name,
            address: place.road_address_name || place.address_name
          })))
        } else {
          console.error('장소 검색 실패:', status)
          setSearchResults([])
          setMapMarkers([])
        }
      })
    } catch (error) {
      console.error('장소 검색 오류:', error)
      setIsSearching(false)
    }
  }

  // 카카오맵 API 로드 확인 함수
  const waitForKakaoAPI = (): Promise<void> => {
    return new Promise((resolve) => {
      const checkAPI = () => {
        if (window.kakao && window.kakao.maps && window.kakao.maps.services) {
          resolve()
        } else {
          setTimeout(checkAPI, 100)
        }
      }
      checkAPI()
    })
  }

  // 검색어 변경 시 장소 검색
  useEffect(() => {
    const timeoutId = setTimeout(async () => {
      if (searchQuery.trim()) {
        await waitForKakaoAPI()
        searchPlaces(searchQuery.trim())
      } else {
        setSearchResults([])
        setMapMarkers([])
      }
    }, 500) // 500ms 디바운스

    return () => clearTimeout(timeoutId)
  }, [searchQuery])

  // 검색 실행 함수
  const executeSearch = async () => {
    if (!searchQuery.trim()) return
    
    await waitForKakaoAPI()
    searchPlaces(searchQuery.trim())
  }

  // 지도 마커 클릭 핸들러
  const handleMarkerClick = (markerData: any) => {
    const place = searchResults.find(p => 
      p.place_name === markerData.title && 
      (p.road_address_name === markerData.address || p.address_name === markerData.address)
    )
    if (place) {
      handlePlaceSelect(place)
    }
  }

  const handlePlaceSelect = (place: PlaceSearchResult) => {
    onBranchInfoChange({
      branchCode: place.id,
      branchName: place.place_name,
      address: place.road_address_name || place.address_name,
      coordinates: {
        latitude: parseFloat(place.y),
        longitude: parseFloat(place.x)
      }
    })
    setSearchQuery(place.place_name)
    setShowMap(false)
  }

  const handleInputChange = (field: keyof BranchInfo, value: string | number) => {
    if (field === 'coordinates') return // 좌표는 직접 수정 불가
    
    onBranchInfoChange({
      ...branchInfo,
      [field]: value
    })
    
    // 에러 메시지 초기화
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: undefined }))
    }
  }

  const validateField = (field: keyof BranchInfo, value: any) => {
    let error = ''
    
    switch (field) {
      case 'branchCode':
        if (!value || !value.trim()) error = '지점코드를 선택해주세요.'
        break
      case 'branchName':
        if (!value || !value.trim()) error = '지점명을 선택해주세요.'
        break
      case 'address':
        if (!value || !value.trim()) error = '주소를 선택해주세요.'
        break
    }
    
    setErrors(prev => ({ ...prev, [field]: error }))
    return !error
  }

  const handleBlur = (field: keyof BranchInfo) => {
    validateField(field, branchInfo[field])
  }

  // 카카오 지도 API 로드 (실제 구현 시)
  useEffect(() => {
    // TODO: 카카오 지도 API 로드
    // const script = document.createElement('script')
    // script.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=${KAKAO_MAP_API_KEY}&autoload=false`
    // document.head.appendChild(script)
  }, [])

  return (
    <div className="w-full max-w-4xl mx-auto p-6">
      <div className="mb-8">
        <h2 className="text-2xl font-['Hana2.0_M'] text-gray-800 mb-2">
          근무 지점 정보
        </h2>
        <p className="text-gray-600 font-['Hana2.0_M'] text-sm">
          근무하시는 지점을 검색하여 선택해주세요.
        </p>
      </div>

      <div className="space-y-6">
        {/* 지점 검색 */}
        <div>
          <label className="block text-sm font-['Hana2.0_M'] text-gray-700 mb-2">
            지점 검색 <span className="text-red-500">*</span>
          </label>
          <div className="flex gap-2">
            <div className="relative flex-1">
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="은행명 또는 지역명으로 검색 (예: 하나은행, 강남구)"
                className="w-full px-4 py-3 border border-gray-300 rounded-lg font-['Hana2.0_M'] text-sm focus:outline-none focus:ring-2 focus:ring-[#008485]"
              />
              <div className="absolute inset-y-0 right-0 flex items-center pr-3">
                {isSearching ? (
                  <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-[#008485]"></div>
                ) : (
                  <svg className="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                  </svg>
                )}
              </div>
            </div>
            <button
              type="button"
              onClick={executeSearch}
              disabled={!searchQuery.trim() || isSearching}
              className={`px-4 py-3 rounded-lg font-['Hana2.0_M'] text-sm transition-colors whitespace-nowrap ${
                !searchQuery.trim() || isSearching
                  ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                  : 'bg-[#008485] text-white hover:bg-[#006666] cursor-pointer'
              }`}
            >
              {isSearching ? '검색 중...' : '검색'}
            </button>
          </div>

          {/* 장소 검색 결과 - input 아래에 표시 */}
          {searchQuery && searchResults.length > 0 && (
            <div className="mt-2 max-h-60 overflow-y-auto border border-gray-200 rounded-lg bg-white shadow-lg">
              <div className="divide-y divide-gray-200">
                {searchResults.map((place) => {
                  const isHanaBank = place.place_name.includes('하나은행')
                  return (
                    <button
                      key={place.id}
                      onClick={() => handlePlaceSelect(place)}
                      className={`w-full text-left p-4 hover:bg-gray-50 transition-colors ${
                        branchInfo.branchCode === place.id ? 'bg-[#008485] text-white' : ''
                      }`}
                    >
                      <div className="flex items-start gap-3">
                        <div className="flex-shrink-0 mt-0.5">
                          {isHanaBank ? (
                            <div className="w-3 h-3 bg-red-500 rounded-full"></div>
                          ) : (
                            <div className="w-3 h-3 bg-gray-300 rounded-full"></div>
                          )}
                        </div>
                        <div className="flex-1">
                          <p className={`font-['Hana2.0_M'] text-sm font-medium ${
                            branchInfo.branchCode === place.id ? 'text-white' : 'text-gray-900'
                          }`}>
                            {place.place_name}
                            {isHanaBank && (
                              <span className="ml-2 px-2 py-0.5 bg-red-100 text-red-600 text-xs rounded-full">
                                하나은행
                              </span>
                            )}
                          </p>
                          <p className={`font-['Hana2.0_M'] text-xs mt-1 ${
                            branchInfo.branchCode === place.id ? 'text-gray-200' : 'text-gray-500'
                          }`}>
                            {place.road_address_name || place.address_name}
                          </p>
                          <p className={`font-['Hana2.0_M'] text-xs mt-1 ${
                            branchInfo.branchCode === place.id ? 'text-gray-200' : 'text-gray-400'
                          }`}>
                            {place.category_name}
                          </p>
                        </div>
                      </div>
                    </button>
                  )
                })}
              </div>
            </div>
          )}

          {searchQuery && !isSearching && searchResults.length === 0 && (
            <div className="mt-2 p-4 text-center text-gray-500 font-['Hana2.0_M'] text-sm border border-gray-200 rounded-lg bg-gray-50">
              검색 결과가 없습니다. 다른 키워드로 검색해보세요.
            </div>
          )}

          {isSearching && (
            <div className="mt-2 p-4 text-center text-gray-500 font-['Hana2.0_M'] text-sm border border-gray-200 rounded-lg bg-gray-50">
              <div className="flex items-center justify-center gap-2">
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-[#008485]"></div>
                검색 중...
              </div>
            </div>
          )}
        </div>

        {/* 지도 영역 */}
        <div className="space-y-4">
          <div className="flex justify-between items-center">
            <label className="block text-sm font-['Hana2.0_M'] text-gray-700">
              지점 위치
            </label>
            <button
              type="button"
              onClick={() => setShowMap(!showMap)}
              className="text-sm text-[#008485] hover:text-[#006666] font-['Hana2.0_M']"
            >
              {showMap ? '지도 닫기' : '지도 보기'}
            </button>
          </div>

          <div className="relative">
            {showMap ? (
              <KakaoMap
                center={branchInfo.branchCode ? {
                  latitude: branchInfo.coordinates.latitude,
                  longitude: branchInfo.coordinates.longitude
                } : {
                  latitude: 37.5665,
                  longitude: 126.9780
                }}
                markers={mapMarkers}
                onMarkerClick={handleMarkerClick}
                height="384px"
              />
            ) : (
              <div className="h-96 bg-gray-50 rounded-lg flex items-center justify-center border-2 border-dashed border-gray-300">
                <div className="text-center">
                  <svg className="w-12 h-12 text-gray-400 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
                  </svg>
                  <p className="text-gray-500 font-['Hana2.0_M'] text-sm">
                    지점을 선택하면 지도가 표시됩니다
                  </p>
                </div>
              </div>
            )}

            {/* 선택된 지점 정보 - 지도 위에 표시 */}
            {branchInfo.branchCode && (
              <div className="absolute top-4 left-4 right-4 bg-white rounded-lg shadow-lg border border-gray-200 p-4">
                <div className="flex items-start justify-between">
                  <div>
                    <h3 className="font-['Hana2.0_M'] text-sm font-medium text-gray-900 mb-1">
                      선택된 지점
                    </h3>
                    <p className="font-['Hana2.0_M'] text-sm text-gray-700 mb-1">
                      {branchInfo.branchName}
                    </p>
                    <p className="font-['Hana2.0_M'] text-xs text-gray-500">
                      {branchInfo.address}
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() => onBranchInfoChange({
                      branchCode: '',
                      branchName: '',
                      address: '',
                      coordinates: { latitude: 0, longitude: 0 }
                    })}
                    className="text-gray-400 hover:text-gray-600"
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
              </div>
            )}
          </div>

        </div>
      </div>

      {/* 안내 메시지 */}
      <div className="mt-6 p-4 bg-blue-50 rounded-lg">
        <div className="flex items-start">
          <div className="flex-shrink-0">
            <svg className="h-5 w-5 text-blue-400" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
            </svg>
          </div>
          <div className="ml-3">
            <p className="text-sm text-blue-700 font-['Hana2.0_M']">
              지역명이나 은행명으로 검색하여 근무 지점을 선택해주세요. 
              하나은행 지점은 빨간색 마커로 표시됩니다.
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Step4CounselorBranchInfo

