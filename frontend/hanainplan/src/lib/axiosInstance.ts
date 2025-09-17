import axios from 'axios';
// import { encryptDataWithRSA } from '../utils/cryptoUtils';

const BASE_URL = 'http://localhost:8080/api/';

const axiosRequestConfig = {
  baseURL: BASE_URL,
  withCredentials: false  // JWT 토큰 없는 개발 버전에서는 false로 설정
};

export const axiosInstance = axios.create(axiosRequestConfig);

// JWT 토큰 없음(개발 버전)

axiosInstance.interceptors.request.use(
  (config) => {
    return config;
  },
  (error) => Promise.reject(error)
);

axiosInstance.interceptors.response.use(
  (res) => res,
  (error) => {
    console.error("API Error:", error.response ?? error.message);
    return Promise.reject(error);
  }
);

// JWT 토큰 추가 시 사용

// export const refreshAccessToken = async () => {
//   try {
//     const response = await axios.post(`${BASE_URL}user/reissue`, {}, {
//       withCredentials: true,
//     });
//     const newAccessToken = response.data.accessToken;
//     localStorage.setItem("ACCESS_TOKEN", newAccessToken);
//     return newAccessToken;
//   } catch (error) {
//     console.log(error)
//     localStorage.removeItem("ACCESS_TOKEN");
//     document.cookie = "refresh=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
//     throw error;
//   }
// };

// //엑세스 토큰 체크 로직
// axiosInstance.interceptors.request.use(
//   async (config) => {
//     const token = localStorage.getItem("ACCESS_TOKEN");
//     if (token) {
//       config.headers.Authorization = token;
//     }

//     if (!config.headers['Content-Type']) {
//       config.headers['Content-Type'] = 'application/json';
//     }
//     console.log(config.data)
//     return config;
//   },
//   (error) => {
//     return Promise.reject(error);
//   }
// );

// axiosInstance.interceptors.response.use(
//   (response) => response,
//   async (error) => {
//     const originalRequest = error.config;
    
//     if (error.response.status === 401 && !originalRequest._retry) {
//       originalRequest._retry = true;
//       window.location.href = '/';
//       const newAccessToken = await refreshAccessToken();
      
//       if (newAccessToken) {
//         localStorage.setItem("ACCESS_TOKEN",newAccessToken);
//         originalRequest.headers.Authorization = newAccessToken;
//         return axiosInstance(originalRequest);
//       }
//     }
    
//     return Promise.reject(error);
//   }
// );