import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Allow CI/verification builds to avoid a .next directory used by a running dev server.
  distDir: process.env.NEXT_DIST_DIR || ".next",
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'placehold.co',
      },
      {
        protocol: 'http',
        hostname: 'localhost',
        port: '8080',
      },
      {
        protocol: 'https',
        hostname: 'localhost',
        port: '8080',
      },
    ],
  },
};

export default nextConfig;
