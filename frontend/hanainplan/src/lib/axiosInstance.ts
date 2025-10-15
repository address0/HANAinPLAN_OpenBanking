import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/';

const axiosRequestConfig = {
  baseURL: BASE_URL,
  withCredentials: false
};

export const axiosInstance = axios.create(axiosRequestConfig);

axiosInstance.interceptors.request.use(
  (config) => {
    return config;
  },
  (error) => Promise.reject(error)
);

axiosInstance.interceptors.response.use(
  (res) => res,
  (error) => {
    return Promise.reject(error);
  }
);