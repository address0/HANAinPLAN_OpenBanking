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

interface PlaceSearchResult {
  id: string;
  place_name: string;
  address_name: string;
  road_address_name: string;
  x: string;
  y: string;
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

  const searchPlaces = async (query: string) => {
    if (!window.kakao || !window.kakao.maps || !window.kakao.maps.services) {
      return
    }

    setIsSearching(true)

    try {
      const ps = new window.kakao.maps.services.Places()

      const searchKeyword = `하나은행 ${query}`

      ps.keywordSearch(searchKeyword, (data: PlaceSearchResult[], status: any) => {
        setIsSearching(false)

        if (status === window.kakao.maps.services.Status.OK) {
          const hanaResults = data.filter(place =>
            place.place_name.includes('하나은행') ||
            place.category_name.includes('은행')
          )

          setSearchResults(hanaResults)

          setMapMarkers(hanaResults.map(place => ({
            latitude: parseFloat(place.y),
            longitude: parseFloat(place.x),
            title: place.place_name,
            address: place.road_address_name || place.address_name
          })))
        } else if (status === window.kakao.maps.services.Status.ZERO_RESULT) {
          setSearchResults([])
          setMapMarkers([])
        } else {
          setSearchResults([])
          setMapMarkers([])
        }
      })
    } catch (error) {
      setIsSearching(false)
      setSearchResults([])
      setMapMarkers([])
    }
  }

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

  useEffect(() => {
    const timeoutId = setTimeout(async () => {
      if (searchQuery.trim()) {
        await waitForKakaoAPI()
        searchPlaces(searchQuery.trim())
      } else {
        setSearchResults([])
        setMapMarkers([])
      }
    }, 500)

    return () => clearTimeout(timeoutId)
  }, [searchQuery])

  const executeSearch = async () => {
    if (!searchQuery.trim()) return

    await waitForKakaoAPI()
    searchPlaces(searchQuery.trim())
  }

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
    setShowMap(true)
    setSearchResults([])
  }

  const handleInputChange = (field: keyof BranchInfo, value: string | number) => {
    if (field === 'coordinates') return

    onBranchInfoChange({
      ...branchInfo,
      [field]: value
    })

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

  useEffect(() => {
  }, [])

  return (
    <div className="w-full max-w-4xl mx-auto p-6">
      <div className="mb-8">
        <h2 className="text-2xl font-['Hana2.0_M'] text-gray-800 mb-2">
          근무 지점 정보 입력
        </h2>
      </div>

      <div className="space-y-6">
        {}
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
                onKeyDown={(e) => {
                  if (e.key === 'Enter' && searchQuery.trim() && !isSearching) {
                    executeSearch()
                  }
                }}
                placeholder="지점명 또는 지역명으로 검색 (예: 강남점, 서울 강남구)"
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

          {}
          {searchQuery && searchResults.length > 0 && (
            <div className="mt-2 max-h-60 overflow-y-auto border border-gray-200 rounded-lg bg-white shadow-lg">
              <div className="divide-y divide-gray-200">
                {searchResults.map((place) => {
                  const isSelected = branchInfo.branchCode === place.id
                  return (
                    <button
                      key={place.id}
                      onClick={() => handlePlaceSelect(place)}
                      className={`w-full text-left p-4 transition-colors ${
                        isSelected
                          ? 'bg-[#008485] text-white'
                          : 'hover:bg-gray-50'
                      }`}
                    >
                      <div className="flex items-start gap-3">
                        <div className="flex-shrink-0 mt-0.5">
                          <div className={`w-3 h-3 rounded-full ${
                            isSelected ? 'bg-white' : 'bg-[#008485]'
                          }`}></div>
                        </div>
                        <div className="flex-1">
                          <div className="flex items-center gap-2">
                            <p className={`font-['Hana2.0_M'] text-sm font-medium ${
                              isSelected ? 'text-white' : 'text-gray-900'
                            }`}>
                              {place.place_name}
                            </p>
                            {!isSelected && (
                              <span className="px-2 py-0.5 bg-[#008485] text-white text-xs rounded-full">
                                하나은행
                              </span>
                            )}
                          </div>
                          <p className={`font-['Hana2.0_M'] text-xs mt-1 ${
                            isSelected ? 'text-gray-100' : 'text-gray-600'
                          }`}>
                            📍 {place.road_address_name || place.address_name}
                          </p>
                        </div>
                        {isSelected && (
                          <div className="flex-shrink-0">
                            <svg className="w-5 h-5 text-white" fill="currentColor" viewBox="0 0 20 20">
                              <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                            </svg>
                          </div>
                        )}
                      </div>
                    </button>
                  )
                })}
              </div>
            </div>
          )}

          {searchQuery && !isSearching && searchResults.length === 0 && !branchInfo.branchCode && (
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

        {}
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

            {}
            {branchInfo.branchCode && (
              <div className="absolute top-4 left-4 right-4 bg-white rounded-lg shadow-xl border-2 border-[#008485] p-4 z-10">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <div className="w-2 h-2 bg-[#008485] rounded-full animate-pulse"></div>
                      <h3 className="font-['Hana2.0_M'] text-xs font-medium text-[#008485] uppercase">
                        선택된 지점
                      </h3>
                    </div>
                    <p className="font-['Hana2.0_M'] text-base font-bold text-gray-900 mb-2">
                      {branchInfo.branchName}
                    </p>
                    <div className="flex items-start gap-2">
                      <svg className="w-4 h-4 text-gray-400 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                      </svg>
                      <p className="font-['Hana2.0_M'] text-xs text-gray-600 leading-relaxed">
                        {branchInfo.address}
                      </p>
                    </div>
                  </div>
                  <button
                    type="button"
                    onClick={() => {
                      onBranchInfoChange({
                        branchCode: '',
                        branchName: '',
                        address: '',
                        coordinates: { latitude: 0, longitude: 0 }
                      })
                      setSearchQuery('')
                      setSearchResults([])
                      setMapMarkers([])
                    }}
                    className="flex-shrink-0 w-8 h-8 flex items-center justify-center rounded-full bg-gray-100 text-gray-400 hover:bg-red-50 hover:text-red-500 transition-colors"
                    title="선택 취소"
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

      {}
      <div className="mt-6 p-4 bg-gradient-to-r from-blue-50 to-cyan-50 rounded-lg border border-blue-100">
        <div className="flex items-start gap-3">
          <div className="flex-shrink-0 mt-0.5">
            <svg className="h-5 w-5 text-[#008485]" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
            </svg>
          </div>
          <div className="flex-1">
            <h4 className="text-sm font-semibold text-[#008485] font-['Hana2.0_M'] mb-1">
              지점 검색 안내
            </h4>
            <ul className="text-sm text-gray-700 font-['Hana2.0_M'] space-y-1">
              <li>• <strong>지점명</strong>으로 검색: "강남점", "서초점", "역삼점" 등</li>
              <li>• <strong>지역명</strong>으로 검색: "강남구", "서초구", "역삼동" 등</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Step4CounselorBranchInfo