// middleware.js
export function middleware(context) {
  const { request, rewrite } = context;
  const url = new URL(request.url);

  // 匹配所有以 /github/ 开头的请求
  if (url.pathname.startsWith('/github/')) {
    // 提取路径，例如 /github/VideoConverter-V0.7.zip  →  VideoConverter-V0.7.zip
    const filePath = url.pathname.replace('/github/', '');
    // 重写到 GitHub 的真实地址
    const gitUrl = `https://github.com/ZONESTAR3D/eEGG-Tools/archive/refs/tags/${filePath}`;
    return rewrite(gitUrl);
  }

  return context.next();
}

export const config = {
  matcher: '/github/:path*',
};