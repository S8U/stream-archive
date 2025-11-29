import Axios, { AxiosRequestConfig } from 'axios';

export const AXIOS_INSTANCE = Axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080',
  withCredentials: true,
});

// 요청 인터셉터: 인증 토큰 추가 등
AXIOS_INSTANCE.interceptors.request.use(
  (config) => {
    // 필요시 토큰 추가
    // const token = localStorage.getItem('token');
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`;
    // }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터: 에러 처리 등
AXIOS_INSTANCE.interceptors.response.use(
  (response) => response,
  (error) => {
    // 에러 처리 로직
    return Promise.reject(error);
  }
);

export const customAxiosInstance = <T>(
  config: AxiosRequestConfig,
  options?: AxiosRequestConfig
): Promise<T> => {
  // params가 중첩된 객체인 경우 플랫하게 변환
  let params = config.params;
  if (params && typeof params === 'object') {
    const flatParams: Record<string, unknown> = {};

    // request, pageable 등의 중첩 객체를 플랫하게 펼침
    Object.entries(params).forEach(([key, value]) => {
      if (value && typeof value === 'object' && !Array.isArray(value)) {
        // 중첩 객체를 플랫하게 펼침
        Object.entries(value as Record<string, unknown>).forEach(([nestedKey, nestedValue]) => {
          if (nestedValue !== undefined && nestedValue !== null) {
            flatParams[nestedKey] = nestedValue;
          }
        });
      } else if (value !== undefined && value !== null) {
        flatParams[key] = value;
      }
    });

    params = flatParams;
  }

  const promise = AXIOS_INSTANCE({
    ...config,
    params,
    ...options,
  }).then(({ data }) => data);

  return promise;
};

export default customAxiosInstance;