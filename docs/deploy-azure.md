# Hướng dẫn Deploy Backend lên Azure VM

## 1. Tạo VM trên Azure Portal

1. Vào https://portal.azure.com → **Virtual Machines** → **Create**
2. Chọn **Resource Group**: `sport-pro-rg` (hoặc tạo mới)
3. **VM Name**: `sport-pro-vtm`
4. **Region**: `Southeast Asia`
5. **Image**: Ubuntu 22.04/24.04 LTS
6. **Size**: B1s (1 vCPU, 1 GB RAM) - tối thiểu
7. **Authentication**: SSH public key
8. Tạo VM và download private key `.pem`

## 2. Kết nối SSH

```bash
ssh -i "path/to/key.pem" azureuser@<VM_IP>
```

## 3. Cài đặt Docker trên VM

```bash
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
```

## 4. Upload file `.env` lên VM

```bash
scp -i key.pem .env azureuser@<VM_IP>:~/app/.env
```

## 5. CI/CD với GitHub Actions

File `.github/workflows/deploy-vm.yml` tự động:

- Build JAR với Maven
- Copy JAR + Dockerfile lên VM
- Build Docker image
- Run container mapping port `80:8080`

### GitHub Secrets cần cấu hình:

| Secret | Giá trị |
|--------|---------|
| `VM_HOST` | IP của VM (VD: `20.189.120.9`) |
| `VM_USER` | `azureuser` |
| `VM_SSH_KEY` | Base64 của file `.pem` |

### Tạo VM_SSH_KEY:

```powershell
$pem = Get-Content "key.pem" -Raw
$bytes = [System.Text.Encoding]::UTF8.GetBytes($pem)
[System.Convert]::ToBase64String($bytes)
```

## 6. Xử lý VM hết RAM

VM B1s chỉ có ~1GB RAM. Java `Xmx600m` + Docker + cloudflared dễ bị OOM.

**Cách fix:** Tạo swap file:

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

## 7. Restart VM

Khi VM bị treo, vào **Azure Portal** → Virtual Machines → `sport-pro-vtm` → **Restart**.

Sau restart, chạy:

```bash
# Kiểm tra Docker container đã tự động chạy chưa
docker ps

# Kiểm tra Cloudflare tunnel
sudo systemctl status cloudflared-tunnel

# Lấy URL tunnel mới (nếu URL cũ không dùng được)
sudo journalctl -u cloudflared-tunnel --no-pager | grep trycloudflare | grep -oP 'https://[a-zA-Z0-9.-]+\.trycloudflare\.com'
```
