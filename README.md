# BSD Sunmi WebView Print (V2 / V2s)

Sunmi POS (V2 / V2s / V2s Plus) এর জন্য কাস্টম WebView অ্যাপ।  
আপনার PHP সফটওয়্যারের `receipt_sunmi.php` পেজ থেকে সরাসরি **built-in thermal printer**-এ প্রিন্ট করে।

## ফিচার

- WebView দিয়ে আপনার ওয়েব অ্যাপ লোড হয়
- JavaScript Bridge: `window.SunmiBridge.printReceipt(text)` এবং `window.lee.funAndroid(text)`
- আপনার বর্তমান `receipt_sunmi.php` কোড **কোনো পরিবর্তন ছাড়াই** কাজ করবে
- Auto-print সাপোর্ট (পেজ লোড হলেই প্রিন্ট)
- Settings থেকে Home URL পরিবর্তন করা যায়
- Test Print বাটন

## প্রয়োজনীয়তা

- Android Studio (Hedgehog / Iguana বা নতুন)
- JDK 17
- আসল **Sunmi V2 / V2s** ডিভাইস (এমুলেটরে প্রিন্টার কাজ করবে না)

## Build করার নিয়ম (GitHub / Local)

### ১. Android Studio দিয়ে

1. প্রজেক্টটি unzip করুন
2. Android Studio → **Open** → ফোল্ডার সিলেক্ট করুন
3. Gradle sync হতে দিন
4. `MainActivity.java` ফাইলে `DEFAULT_HOME_URL` পরিবর্তন করুন আপনার সাইটের URL দিয়ে:

```java
private static final String DEFAULT_HOME_URL = "https://your-domain.com/";
```

5. USB দিয়ে Sunmi ডিভাইস কানেক্ট করুন (Developer options + USB debugging চালু)
6. **Run** বাটন চাপুন → APK ইনস্টল হবে

### ২. Command line দিয়ে

```bash
cd SunmiWebViewPrint
./gradlew assembleDebug
# APK পাবেন: app/build/outputs/apk/debug/app-debug.apk
```

Release APK:

```bash
./gradlew assembleRelease
```

### ৩. GitHub Actions দিয়ে (ঐচ্ছিক)

রিপোজিটরিতে এই প্রজেক্ট আপলোড করুন, তারপর Actions দিয়ে build করতে পারেন।

## PHP সাইড (আপনার কোড)

আপনার `receipt_sunmi.php` ইতিমধ্যে সঠিক। শুধু নিশ্চিত করুন যে:

```js
window.SunmiBridge.printReceipt(text);
// অথবা
window.lee.funAndroid(text);
```

এই অ্যাপে দুটোই সাপোর্ট করা হয়েছে।

## অ্যাপে প্রথমবার খোলার পর

1. অ্যাপ ওপেন করুন
2. উপরের **Settings (gear)** আইকনে চাপুন
3. আপনার ওয়েবসাইটের URL দিন (যেমন `https://billing.yourdomain.com/`)
4. Save করুন
5. কাস্টমার লিস্ট থেকে Receipt খুললেই auto-print হবে

## ট্রাবলশুটিং

| সমস্যা | সমাধান |
|--------|--------|
| Printer not ready | আসল Sunmi ডিভাইসে চালান, এমুলেটরে নয় |
| Bridge পাওয়া যায়নি | পেজটি এই অ্যাপের WebView-এর ভিতরে খুলুন |
| কিছু প্রিন্ট হয় না | কাগজ আছে কিনা দেখুন, Settings → Test Print চেষ্টা করুন |
| HTTP সাইট লোড হয় না | Manifest-এ `usesCleartextTraffic="true"` আছে, ঠিক আছে |

## প্যাকেজ

- `applicationId`: `com.bsd.sunmiprint`
- Min SDK: 24 (Android 7.0)
- Target SDK: 34

## লাইসেন্স

আপনার নিজের প্রজেক্টের জন্য ফ্রি ব্যবহার করতে পারেন।
