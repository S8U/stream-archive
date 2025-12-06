import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  images: {
    remotePatterns: [
      {
        protocol: (process.env.NEXT_PUBLIC_API_IMAGE_PROTOCOL as 'http' | 'https') || 'http',
        hostname: process.env.NEXT_PUBLIC_API_IMAGE_HOSTNAME || 'localhost',
        port: process.env.NEXT_PUBLIC_API_IMAGE_PORT || '8080',
        pathname: '/videos/**',
      },
      {
        protocol: (process.env.NEXT_PUBLIC_API_IMAGE_PROTOCOL as 'http' | 'https') || 'http',
        hostname: process.env.NEXT_PUBLIC_API_IMAGE_HOSTNAME || 'localhost',
        port: process.env.NEXT_PUBLIC_API_IMAGE_PORT || '8080',
        pathname: '/channels/**',
      },
    ],
  },
};

export default nextConfig;
