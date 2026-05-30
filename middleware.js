// middleware.js
export function middleware(context) {
  const { request, rewrite } = context;
  const url = new URL(request.url);

  // 匹配所有以 /download/ 开头的请求
  if (url.pathname.startsWith('/download/')) {
    // 提取请求路径，例如 /download/apk/file.apk  → /apk/file.apk
    const cosPath = url.pathname.replace('/download', '');
    // 重写到 COS 的真实地址
    const cosUrl = `https://eegg-1331241757.cos.ap-hongkong.myqcloud.com${cosPath}`;
    return rewrite(cosUrl);
  }

  // 其他请求正常处理
  return context.next();
}

// 可选：限制中间件只在 /download 路径下生效
export const config = {
  matcher: '/download/:path*',
};