import { axiosInstance } from "./axiosInstance";
import type { AxiosRequestConfig } from "axios";

export async function httpGet<TResponse, TParams = unknown>(
  url: string,
  params?: TParams,
  config?: AxiosRequestConfig
): Promise<TResponse> {
  const res = await axiosInstance.get<TResponse>(url, { params, ...config });
  return res.data;
}

export async function httpPost<TResponse, TBody = unknown>(
  url: string,
  body?: TBody,
  config?: AxiosRequestConfig
): Promise<TResponse> {
  const res = await axiosInstance.post<TResponse>(url, body, config);
  return res.data;
}

export async function httpPut<TResponse, TBody = unknown>(
  url: string,
  body?: TBody,
  config?: AxiosRequestConfig
): Promise<TResponse> {
  const res = await axiosInstance.put<TResponse>(url, body, config);
  return res.data;
}

export async function httpDelete<TResponse, TParams = unknown>(
  url: string,
  params?: TParams,
  config?: AxiosRequestConfig
): Promise<TResponse> {
  const res = await axiosInstance.delete<TResponse>(url, { params, ...config });
  return res.data;
}