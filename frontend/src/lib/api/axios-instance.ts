import Axios, { AxiosError, AxiosRequestConfig, InternalAxiosRequestConfig } from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

export const AXIOS_INSTANCE = Axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
});

// 토큰 리프레시 상태 관리
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: unknown) => void;
  reject: (reason?: unknown) => void;
  config: InternalAxiosRequestConfig;
}> = [];

const processQueue = (error: AxiosError | null) => {
  failedQueue.forEach(({ resolve, reject, config }) => {
    if (error) {
      reject(error);
    } else {
      resolve(AXIOS_INSTANCE(config));
    }
  });
  failedQueue = [];
};

// 401 발생 시 자동 refresh
AXIOS_INSTANCE.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    // 401이 아니거나, 이미 재시도했거나, refresh 요청 자체인 경우 그냥 에러 반환
    if (
      error.response?.status !== 401 ||
      originalRequest._retry ||
      originalRequest.url?.includes('/auth/refresh')
    ) {
      return Promise.reject(error);
    }

    // 이미 refresh 중이면 큐에 추가
    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject, config: originalRequest });
      });
    }

    originalRequest._retry = true;
    isRefreshing = true;

    try {
      await AXIOS_INSTANCE.post('/auth/refresh', {});
      processQueue(null);
      return AXIOS_INSTANCE(originalRequest);
    } catch (refreshError) {
      processQueue(refreshError as AxiosError);
      
      // /users/me는 인증 체크용이므로 리다이렉트하지 않음
      const isAuthCheck = originalRequest.url?.includes('/users/me');
      if (typeof window !== 'undefined' && !isAuthCheck && !window.location.pathname.includes('/login')) {
        window.location.href = '/login';
      }
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  }
);

// Orval용 커스텀 인스턴스 (params 플랫화)
export const customAxiosInstance = <T>(
  config: AxiosRequestConfig,
  options?: AxiosRequestConfig
): Promise<T> => {
  let params = config.params;
  
  if (params && typeof params === 'object') {
    const flatParams: Record<string, unknown> = {};
    Object.entries(params).forEach(([key, value]) => {
      if (value && typeof value === 'object' && !Array.isArray(value)) {
        Object.entries(value as Record<string, unknown>).forEach(([k, v]) => {
          if (v !== undefined && v !== null) flatParams[k] = v;
        });
      } else if (value !== undefined && value !== null) {
        flatParams[key] = value;
      }
    });
    params = flatParams;
  }

  return AXIOS_INSTANCE({ ...config, params, ...options }).then(({ data }) => data);
};

export default customAxiosInstance;