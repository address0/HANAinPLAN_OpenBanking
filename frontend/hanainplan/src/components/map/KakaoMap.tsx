import React, { useEffect, useRef } from 'react';

interface KakaoMapProps {
  center: {
    latitude: number;
    longitude: number;
  };
  markers: Array<{
    latitude: number;
    longitude: number;
    title: string;
    address: string;
  }>;
  onMarkerClick: (markerData: any) => void;
  height: string;
}

declare global {
  interface Window {
    kakao: any;
  }
}

const KakaoMap: React.FC<KakaoMapProps> = ({ center, markers, onMarkerClick, height }) => {
  const mapRef = useRef<HTMLDivElement>(null);
  const mapInstanceRef = useRef<any>(null);
  const markerInstancesRef = useRef<any[]>([]);
  const [isLoading, setIsLoading] = React.useState(true);
  const [error, setError] = React.useState<string | null>(null);

  useEffect(() => {
    const apiKey = import.meta.env.VITE_KAKAO_API_KEY;

    if (!apiKey) {
      setError('카카오 API 키가 설정되지 않았습니다.');
      setIsLoading(false);
      return;
    }

    const initMapWithDelay = () => {
      setTimeout(() => {
        if (!mapRef.current) {
          setTimeout(initMapWithDelay, 100);
          return;
        }

        if (window.kakao && window.kakao.maps) {
          initMap();
          setIsLoading(false);
          return;
        }

    const script = document.createElement('script');
    script.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=${apiKey}&libraries=services&autoload=false`;
    script.async = true;

        script.onload = () => {
          window.kakao.maps.load(() => {
            if (window.kakao && window.kakao.maps) {
              initMap();
              setIsLoading(false);
            } else {
              setError('카카오 지도 SDK 로드에 실패했습니다.');
              setIsLoading(false);
            }
          });
        };

        script.onerror = () => {
          setError('카카오 지도 SDK 로드 중 오류가 발생했습니다.');
          setIsLoading(false);
        };

        document.head.appendChild(script);
      }, 50);
    };

    initMapWithDelay();
  }, []);

  const initMap = () => {
    if (!mapRef.current || !window.kakao?.maps) return;

    const mapOption = {
      center: new window.kakao.maps.LatLng(center.latitude, center.longitude),
      level: 3
    };

    const map = new window.kakao.maps.Map(mapRef.current, mapOption);
    mapInstanceRef.current = map;

    updateMarkers();
  };

  const updateMarkers = () => {
    if (!window.kakao?.maps || !mapInstanceRef.current) return;

    markerInstancesRef.current.forEach(marker => marker.setMap(null));
    markerInstancesRef.current = [];

    markers.forEach((markerData) => {
      const markerPosition = new window.kakao.maps.LatLng(
        markerData.latitude,
        markerData.longitude
      );

      const marker = new window.kakao.maps.Marker({
        position: markerPosition,
        map: mapInstanceRef.current
      });

      const infowindow = new window.kakao.maps.InfoWindow({
        content: `
          <div style="padding: 10px; min-width: 150px;">
            <div style="font-weight: bold; margin-bottom: 5px;">${markerData.title}</div>
            <div style="font-size: 12px; color: #666;">${markerData.address}</div>
          </div>
        `
      });

      window.kakao.maps.event.addListener(marker, 'click', () => {
        infowindow.open(mapInstanceRef.current, marker);
        onMarkerClick(markerData);
      });

      markerInstancesRef.current.push(marker);
    });
  };

  useEffect(() => {
    if (mapInstanceRef.current && window.kakao?.maps) {
      const newCenter = new window.kakao.maps.LatLng(center.latitude, center.longitude);
      mapInstanceRef.current.setCenter(newCenter);
      updateMarkers();
    }
  }, [center, markers, onMarkerClick]);

  return (
    <div className="w-full rounded-lg overflow-hidden" style={{ height }}>
      {}
      <div
        ref={mapRef}
        className="w-full h-full"
        style={{ height }}
      />

      {}
      {isLoading && (
        <div className="absolute inset-0 bg-gray-100 flex items-center justify-center border-2 border-dashed border-gray-300">
          <div className="text-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-[#008485] mx-auto mb-2"></div>
            <p className="text-gray-500 font-['Hana2.0_M'] text-sm">
              지도를 불러오는 중...
            </p>
            <p className="text-gray-400 font-['Hana2.0_M'] text-xs mt-1">
              개발자 도구 콘솔을 확인해주세요
            </p>
          </div>
        </div>
      )}

      {}
      {error && (
        <div className="absolute inset-0 bg-red-50 flex items-center justify-center border-2 border-dashed border-red-200">
          <div className="text-center">
            <svg className="w-12 h-12 text-red-400 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <p className="text-red-600 font-['Hana2.0_M'] text-sm">
              {error}
            </p>
            <p className="text-red-400 font-['Hana2.0_M'] text-xs mt-1">
              API 키를 확인해주세요.
            </p>
          </div>
        </div>
      )}
    </div>
  );
};

export default KakaoMap;