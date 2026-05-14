# 📱 AssistX Monitor - Android Application

Aplikasi Android native untuk monitoring POC 24/7 yang terintegrasi dengan dashboard web **AssistX Monitoring V2**.

## ✨ Fitur Utama

### 1. Live Monitoring (Dashboard)
- **Real-time sync** via Server-Sent Events (SSE) dari dashboard web
- **Polling fallback** otomatis jika SSE terputus
- Kartu perangkat dengan indikator online/offline, CPU, RAM, dan kuota
- Filter chip (Semua / Online / Offline / Peringatan)
- Search bar untuk mencari perangkat
- Pull-to-refresh

### 2. Aktivitas (History)
- Timeline aktivitas harian / mingguan / bulanan
- Grafik ringkasan online/offline per periode
- Data CPU & Memory rata-rata

### 3. Notifikasi (Alerts)
- Daftar semua perangkat yang perlu perhatian
- Offline alert, kuota kritis (<1GB), dan kuota rendah (<5GB)
- Prioritas berdasarkan severity

### 4. Pengaturan (Settings)
- Konfigurasi server URL
- Dark mode toggle
- Alert popup toggle
- Interval polling (3-30 detik)

---

## 🚀 Fitur 24/7 Background Service

### Foreground Service
- **MonitoringService** berjalan sebagai foreground service dengan notifikasi silent
- Tetap berjalan meski aplikasi ditutup (START_STICKY)
- WakeLock untuk mencegah CPU deep sleep
- Auto-restart setelah reboot (BootReceiver)

### Popup Alert di Background
- **AlertPopupActivity** muncul di atas semua aplikasi, termasuk lock screen
- Menggunakan SYSTEM_ALERT_WINDOW permission
- Menyala layar jika terkunci
- Alert 3 jenis: OFFLINE, RECOVERED, QUOTA CRITICAL

### Push Notification (FCM)
- Firebase Cloud Messaging untuk notifikasi dari server
- 2 channel: service (silent) dan alerts (priority high + vibrate)

---

## 📂 Struktur Project

```
AndroidApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/assistx/monitor/
│   │   │   ├── AssistXApplication.kt        # Application class
│   │   │   ├── service/
│   │   │   │   ├── MonitoringService.kt      # Foreground 24/7 service
│   │   │   │   ├── AlertManager.kt           # Popup & notification
│   │   │   │   ├── BootReceiver.kt           # Auto-start after boot
│   │   │   │   └── AssistXFCMService.kt      # Firebase messaging
│   │   │   ├── sync/
│   │   │   │   └── SSEManager.kt             # Server-Sent Events
│   │   │   ├── network/
│   │   │   │   ├── ApiClient.kt              # Retrofit + OkHttp
│   │   │   │   └── ApiService.kt             # REST endpoints
│   │   │   ├── data/
│   │   │   │   ├── model/PcDevice.kt         # Data model
│   │   │   │   └── local/PreferencesManager.kt # DataStore
│   │   │   └── ui/
│   │   │       ├── MainActivity.kt           # Bottom nav host
│   │   │       ├── popup/AlertPopupActivity.kt
│   │   │       ├── dashboard/
│   │   │       │   ├── DashboardFragment.kt
│   │   │       │   ├── DashboardViewModel.kt
│   │   │       │   └── DeviceAdapter.kt
│   │   │       ├── history/
│   │   │       │   ├── HistoryFragment.kt
│   │   │       │   ├── HistoryViewModel.kt
│   │   │       │   └── HistoryAdapter.kt
│   │   │       ├── alerts/
│   │   │       │   ├── AlertsFragment.kt
│   │   │       │   ├── AlertsViewModel.kt
│   │   │       │   └── AlertsAdapter.kt
│   │   │       └── settings/
│   │   │           ├── SettingsFragment.kt
│   │   │           └── SettingsViewModel.kt
│   │   ├── res/
│   │   │   ├── layout/          # 8 XML layout files
│   │   │   ├── drawable/        # Icons & shapes
│   │   │   ├── navigation/      # Nav graph
│   │   │   ├── menu/            # Bottom nav menu
│   │   │   ├── values/          # Colors, strings, themes
│   │   │   ├── values-night/    # Dark mode colors
│   │   │   └── xml/             # Network config
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🔧 Build & Run

### Prerequisites
- Android Studio Hedgehog (2023.1+) atau Giraffe
- JDK 17
- Android SDK 34
- Gradle 8.4+

### Setup
1. Buka folder `AndroidApp/` di Android Studio
2. Sync Gradle
3. Tambahkan `google-services.json` dari Firebase Console ke `app/`
4. Build & Run

### APK Build
```bash
cd AndroidApp
./gradlew assembleRelease
```
APK akan tersedia di `app/build/outputs/apk/release/`

---

## 🔗 Integrasi dengan Dashboard Web

Aplikasi ini mengambil data dari API dashboard web:
- `GET /api/clients` — daftar perangkat
- `GET /api/stream` — Server-Sent Events real-time
- `GET /api/analytics/summary?range=daily|weekly|monthly` — history
- `GET /api/clients/{id}/history` — history per perangkat

**Default server:** `http://192.168.1.100:5060` (bisa diubah di Settings)

---

## 🎨 Design System

### Colors (sesuai blueprint)
| Nama | Hex | Penggunaan |
|------|-----|-----------|
| Brand Blue | `#0052CC` | Primary accent |
| Success Green | `#22C55E` | Online / Normal |
| Warning Orange | `#F59E0B` | Kuota rendah |
| Danger Red | `#EF4444` | Offline |

### Typography
- Roboto (sans-serif) untuk keterbacaan maksimal
- Judul: 16-18sp Medium
- Body: 12-14sp Regular

### Theme Support
- Light Theme: `Theme.AssistXMonitor`
- Dark Theme: `Theme.AssistXMonitor.Dark`

---

## 📋 Permission Required
- `INTERNET` — komunikasi dashboard
- `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_DATA_SYNC` — 24/7 service
- `POST_NOTIFICATIONS` — push notification
- `WAKE_LOCK` — mencegah deep sleep
- `RECEIVE_BOOT_COMPLETED` — auto-start
- `SYSTEM_ALERT_WINDOW` — popup di atas lock screen

---

Built with ❤️ by **AssistX Enterprise AI**
