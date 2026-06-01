# Hướng dẫn Cloudflare Tunnel

## Kiến trúc

```
User (HTTPS) → Cloudflare Edge → Cloudflare Tunnel → localhost:80 → Docker container Java
```

## 1. Cài đặt cloudflared trên VPS

```bash
curl -sL https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64 -o /tmp/cloudflared
chmod +x /tmp/cloudflared
sudo mv /tmp/cloudflared /usr/local/bin/cloudflared
```

## 2. Tạo Cloudflare Account

1. Vào https://dash.cloudflare.com/sign-up
2. Đăng ký email → xác nhận
3. Vào **My Profile** → **API Tokens** → **Create Token**
4. Chọn **Create Custom Token**
5. Permissions: `Account` → `Cloudflare Tunnel` → `Edit`
6. Click **Create Token**, copy token (`cfut_...`)

## 3. Login cloudflared

```bash
cloudflared tunnel login
# Mở link hiện ra trên trình duyệt -> đăng nhập Cloudflare -> Authorize
```

Hoặc dùng token:

```bash
cloudflared tunnel login --token cfut_xxxxx
```

## 4. Khởi động Quick Tunnel (không cần domain)

```bash
cloudflared tunnel --url http://localhost:80
```

Output sẽ hiện URL dạng: `https://xxx.trycloudflare.com`

## 5. Cài đặt tự động (systemd service)

```bash
sudo tee /etc/systemd/system/cloudflared-tunnel.service > /dev/null << 'EOF'
[Unit]
Description=Cloudflare Tunnel - sport-pro-be
After=network.target

[Service]
Type=simple
User=azureuser
ExecStart=/usr/local/bin/cloudflared tunnel --url http://localhost:80
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable cloudflared-tunnel
sudo systemctl start cloudflared-tunnel
```

## 6. Khi Restart VPS

### Bước 1: Restart VM trên Azure

### Bước 2: Kiểm tra service đã chạy chưa

```bash
sudo systemctl status cloudflared-tunnel
```

### Bước 3: Lấy URL tunnel mới

```bash
sudo journalctl -u cloudflared-tunnel --no-pager | grep trycloudflare | grep -oP 'https://[a-zA-Z0-9.-]+\.trycloudflare\.com'
```

URL sẽ có dạng: `https://xxxxx.trycloudflare.com`

### Bước 4: Cập nhật URL trên Vercel

1. Vào **https://vercel.com** → project `sport-pro-fe`
2. **Settings** → **Environment Variables**
3. Sửa giá trị `NEXT_PUBLIC_API_URL` thành URL mới (thêm `/api` ở cuối)
   - VD: `https://xxxxx.trycloudflare.com/api`
4. **Save**
5. **Deployments** → **Redeploy**

### Bước 5: (Tùy chọn) Cập nhật FE local

```bash
# Trong file .env.local của frontend
NEXT_PUBLIC_API_URL=https://xxxxx.trycloudflare.com/api
```

## 7. Script tiện ích

Để kiểm tra URL nhanh:

```bash
# Trên VPS
sudo journalctl -u cloudflared-tunnel --no-pager | grep -oP 'https://[a-zA-Z0-9.-]+\.trycloudflare\.com' | tail -1
```

## 8. Nâng cấp: Dùng domain riêng

Để tránh URL đổi mỗi lần restart, mua domain (VD: `sportpro.shop` ~ $1-2):

1. Add domain vào Cloudflare
2. Tạo named tunnel:
   ```bash
   cloudflared tunnel create sport-pro-be
   ```
3. Trỏ DNS: `api.sportpro.shop` → CNAME → tunnel
4. Dùng URL cố định: `https://api.sportpro.shop`
