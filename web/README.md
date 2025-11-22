diff --git a/README.md b/README.md
index aabf9c7ba9d720dab3320f8d4df597fc5cfdc088..2619b5641834f6ec3de591b0f8d3a305c1c5fc09 100644
--- a/README.md
+++ b/README.md
@@ -1 +1,51 @@
-# yolkargo1
\ No newline at end of file
+# YolKargo Platformu
+
+YolKargo, şehirler arası seyahat eden sürücüler ile uygun fiyata kargo göndermek isteyen kişileri buluşturan bir pazar yeri fikridir. Bu depo, fikrin hızlıca test edilebilmesi için basit bir web tanıtım sitesi ve Expo tabanlı mobil uygulama prototipi içerir.
+
+## İçerik
+
+- `web/`: YolKargo hizmetini tanıtan statik web sitesi.
+- `mobile/`: Expo ile çalışacak React Native mobil uygulama prototipi.
+
+## Web Sitesi
+
+Statik web sitesi, hizmetin değer teklifini ve özelliklerini anlatan tek sayfalık bir tasarımdır.
+
+### Çalıştırma
+
+```bash
+cd web
+python3 -m http.server 8000
+```
+
+Ardından tarayıcınızda `http://localhost:8000` adresine gidin.
+
+## Mobil Uygulama
+
+Mobil uygulama, sürücü ve gönderici akışlarını görselleştiren temel bir prototiptir. Proje Expo ile uyumludur.
+
+### Kurulum
+
+1. [Node.js](https://nodejs.org/) ve [Expo CLI](https://docs.expo.dev/get-started/installation/) kurulu olmalıdır.
+2. Bağımlılıkları yükleyin:
+
+   ```bash
+   cd mobile
+   npm install
+   ```
+
+### Çalıştırma
+
+```bash
+npm start
+```
+
+Komut sonrası Expo geliştirici aracından QR kodu tarayarak uygulamayı fiziksel cihazınızda veya emülatörde test edebilirsiniz.
+
+## Sonraki Adımlar
+
+- Firebase veya benzeri bir arka uç hizmeti ile kimlik doğrulama ve gerçek zamanlı veri yönetimi eklemek.
+- Stripe / iyzico gibi bir ödeme altyapısı entegre etmek.
+- Sürücüler ve göndericiler için iki yönlü puanlama sistemi geliştirmek.
+- Canlı konum takibi için Google Maps API entegrasyonu yapmak.
+- MVP aşamasında Telegram/WhatsApp üzerinden topluluk oluşturup talep toplamak.
# GoGo Platformu

GoGo, şehirler arası seyahat eden sürücüler ile uygun maliyetli teslimat çözümleri arayan göndericileri buluşturan dijital bir pazaryeridir. Depo, web tanıtım sitesi, Expo tabanlı mobil prototip ve yeni Android Studio projesini içerir.

## İçerik

- `web/`: Hizmetin kamuya açık tanıtım sitesi.
- `mobile/`: Expo ile çalışan React Native prototipi.
- `android/`: Jetpack Compose ile yazılmış yerel Android uygulaması.

## Web Sitesi

Statik web sitesi, hizmetin değer teklifini, sürücü/gönderici panellerini ve canlı ilanları anlatan tek sayfalık bir deneyim sunar.

### Çalıştırma

```bash
cd web
python3 -m http.server 8000
```

Tarayıcıdan `http://localhost:8000` adresine giderek sayfayı görüntüleyebilirsiniz.

## Mobil Uygulamalar

### Expo Prototipi

React Native tabanlı prototip, temel form ve akışları doğrulamak için kullanılabilir.

```bash
cd mobile
npm install
npm start
```

Komut sonrası Expo geliştirici aracındaki QR kodu telefonunuzla tarayarak uygulamayı deneyebilirsiniz.

### Android Studio Projesi

`android/` klasörü, sürücü/gönderici kimlik doğrulaması, ilan filtreleme, ilan oluşturma, mesajlaşma ve randevu planlama özelliklerine sahip Jetpack Compose uygulamasını barındırır.

Projeyi açmak için Android Studio > **Open Existing Project** ile `android/` dizinini seçin. Komut satırından bağımlılıkları hazırlamak isterseniz:

```bash
cd android
./gradlew tasks
```

Android Studio içinde `Run` butonuna tıklayarak emülatörde veya cihazda uygulamayı test edebilirsiniz.

#### Android Studio'da adım adım kurulum
1. **Projeyi alın:** Bu depoyu bilgisayarınıza klonlayın ya da arşiv olarak indirip çıkartın.
2. **Android Studio'yu açın:** Açılış ekranında **Open**/**Open Existing Project** seçeneğiyle deponun içindeki `android/` klasörünü gösterin.
3. **SDK konumunu doğrulayın:** `File > Settings (macOS'ta Preferences) > Appearance & Behavior > System Settings > Android SDK` menüsünden SDK'nın kurulu olduğundan emin olun; gerekirse `SDK Tools` sekmesinden Android SDK Build-Tools ve Platform-Tools'u yükleyin.
4. **Gradle senkronizasyonu:** Proje açıldığında üstteki sarı çubukta çıkan **Sync Now** uyarısına tıklayın; ayrıca araç çubuğundaki fil dişi simgesinden manuel olarak da `Sync Project with Gradle Files` yapabilirsiniz.
5. **Çalıştırma hedefini seçin:** Araç çubuğundaki `Run/Debug Configurations` açılırından **app** modülünü seçin; yanındaki cihaz açılır menüsünden takılı fiziksel cihazı ya da AVD Manager üzerinden oluşturduğunuz emülatörü belirleyin.
6. **Uygulamayı başlatın:** `Run ▶` butonuna tıklayın. İlk çalıştırmada derleme süreleri uzun olabilir; tamamlandığında uygulama seçtiğiniz cihazda açılır.
7. **Hızlı doğrulama:** Rol seçimi, giriş/kayıt, ilan listesi, filtreler, ilan oluşturma, mesaj ve randevu akışları yerel verilerle çalışır. Test sonrası `Settings > Apps > GoGo > Storage > Clear data` adımlarıyla oturumu temizleyebilirsiniz.

## Sonraki Adımlar

- Firebase veya Supabase ile gerçek zamanlı veri & kimlik doğrulama.
- Stripe/iyzico ödeme entegrasyonu.
- Puanlama/yorum sistemi.
- Google Maps tabanlı canlı konum paylaşımı.
