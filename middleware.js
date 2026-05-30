// middleware.js
export function middleware(context) {
  const { request, rewrite } = context;
  const url = new URL(request.url);

  // 匹配所有以 /download/ 开头的请求
  if (url.pathname.startsWith('/download/')) {
    // 提取请求路径，例如 /download/apk/file.apk  → /apk/file.apk
    const cosPath = url.pathname.replace('/download', '');
    // 使用自定义域名（绑定后生效）
    const cosUrl = `https://download.zonestar3d.com${cosPath}`;
    return rewrite(cosUrl);
  }

  return context.next();
}

export const config = {
  matcher: '/download/:path*',
};