<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>下载应用 - 您的产品名称</title>
    <script src="https://cdn.jsdelivr.net/npm/qrcode@1.5.3/build/qrcode.min.js"></script>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Segoe UI', 'Microsoft YaHei', sans-serif;
        }
        
        body {
            background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
            color: #333;
            line-height: 1.6;
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
        }
        
        .container {
            max-width: 800px;
            width: 100%;
            background: white;
            border-radius: 20px;
            box-shadow: 0 15px 30px rgba(0, 0, 0, 0.2);
            overflow: hidden;
        }
        
        header {
            background: #2575fc;
            color: white;
            padding: 30px 20px;
            text-align: center;
        }
        
        header h1 {
            font-size: 2.2rem;
            margin-bottom: 10px;
            font-weight: 700;
        }
        
        header p {
            font-size: 1.1rem;
            opacity: 0.9;
        }
        
        .content {
            padding: 40px 30px;
            display: flex;
            flex-wrap: wrap;
            gap: 30px;
        }
        
        .qr-section {
            flex: 1;
            min-width: 300px;
            text-align: center;
            padding: 20px;
            border-right: 1px dashed #e0e0e0;
        }
        
        .info-section {
            flex: 1;
            min-width: 300px;
            padding: 10px;
        }
        
        #qrcode {
            display: inline-block;
            background: white;
            padding: 20px;
            border-radius: 12px;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
            margin-bottom: 20px;
        }
        
        .instructions {
            background: #f8f9fa;
            padding: 25px;
            border-radius: 12px;
            margin-top: 20px;
        }
        
        .instructions h3 {
            color: #2575fc;
            margin-bottom: 15px;
            font-size: 1.3rem;
        }
        
        .instructions ol {
            padding-left: 20px;
        }
        
        .instructions li {
            margin-bottom: 12px;
        }
        
        .download-btn {
            display: inline-block;
            background: #2575fc;
            color: white;
            padding: 14px 30px;
            border-radius: 50px;
            text-decoration: none;
            font-weight: 600;
            font-size: 1.1rem;
            margin-top: 20px;
            transition: all 0.3s ease;
            box-shadow: 0 4px 15px rgba(37, 117, 252, 0.4);
        }
        
        .download-btn:hover {
            background: #1c68e0;
            transform: translateY(-3px);
            box-shadow: 0 6px 20px rgba(37, 117, 252, 0.5);
        }
        
        .app-info {
            margin-top: 25px;
            padding-top: 20px;
            border-top: 1px solid #eaeaea;
        }
        
        .app-info h3 {
            color: #2575fc;
            margin-bottom: 10px;
        }
        
        .app-details {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 10px;
            margin-top: 15px;
        }
        
        .detail-item {
            padding: 8px 0;
        }
        
        .detail-label {
            font-weight: 600;
            color: #555;
        }
        
        footer {
            text-align: center;
            padding: 20px;
            background: #f8f9fa;
            color: #666;
            font-size: 0.9rem;
        }
        
        @media (max-width: 768px) {
            .qr-section {
                border-right: none;
                border-bottom: 1px dashed #e0e0e0;
                padding-bottom: 30px;
            }
            
            .content {
                padding: 30px 20px;
            }
            
            header h1 {
                font-size: 1.8rem;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>下载应用</h1>
            <p>扫描下方二维码，立即安装应用</p>
        </header>
        
        <div class="content">
            <div class="qr-section">
                <div id="qrcode"></div>
                <p>使用手机扫描二维码下载应用</p>
                
                <div class="instructions">
                    <h3>安装说明</h3>
                    <ol>
                        <li>使用手机摄像头或扫码工具扫描上方二维码</li>
                        <li>点击下载链接，等待应用下载完成</li>
                        <li>打开下载的APK文件进行安装</li>
                        <li>如有安全提示，请选择"继续安装"或"允许安装"</li>
                        <li>安装完成后即可打开应用</li>
                    </ol>
                </div>
            </div>
            
            <div class="info-section">
                <h2>应用信息</h2>
                <p>感谢您下载我们的应用！此应用将为您提供出色的用户体验和丰富的功能。</p>
                
                <div class="app-info">
                    <h3>应用详情</h3>
                    <div class="app-details">
                        <div class="detail-item">
                            <span class="detail-label">应用名称:</span> 您的产品名称
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">版本:</span> 1.0.0
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">文件大小:</span> 约 25 MB
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">系统要求:</span> Android 5.0+
                        </div>
                    </div>
                </div>
                
                <a href="#" class="download-btn" id="directDownload">直接下载APK</a>
                
                <div class="instructions" style="margin-top: 30px;">
                    <h3>注意事项</h3>
                    <ul>
                        <li>请确保手机有足够的存储空间</li>
                        <li>建议在Wi-Fi环境下下载以节省流量</li>
                        <li>安装前请确保已开启"允许安装来自未知来源的应用"</li>
                        <li>如有问题，请联系我们的技术支持</li>
                    </ul>
                </div>
            </div>
        </div>
        
        <footer>
            <p>&copy; 2023 您的公司名称. 保留所有权利.</p>
        </footer>
    </div>

    <script>
        // 生成二维码
        document.addEventListener('DOMContentLoaded', function() {
            // 请将下面的URL替换为您的APK文件实际URL
            const apkUrl = 'https://example.com/your-app.apk';
            
            // 生成二维码
            QRCode.toCanvas(document.getElementById('qrcode'), apkUrl, {
                width: 200,
                margin: 1,
                color: {
                    dark: '#000000',
                    light: '#FFFFFF'
                }
            }, function(error) {
                if (error) console.error(error);
            });
            
            // 设置直接下载链接
            document.getElementById('directDownload').href = apkUrl;
        });
    </script>
</body>
</html>
