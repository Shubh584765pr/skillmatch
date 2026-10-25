import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Docker image needs standalone; Vercel uses its own Next.js runtime.
  ...(process.env.VERCEL ? {} : { output: "standalone" as const }),
};

export default nextConfig;
