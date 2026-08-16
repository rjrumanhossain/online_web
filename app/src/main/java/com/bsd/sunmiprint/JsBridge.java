package com.bsd.sunmiprint;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * JavaScript interface exposed to WebView.
 * Supports both:
 *   window.SunmiBridge.printReceipt(text)
 *   window.lee.funAndroid(text)
 * so that existing PHP receipt page works without change.
 */
public class JsBridge {

    private static final String TAG = "JsBridge";

    private final MainActivity activity;
    private final SunmiPrinterHelper printerHelper;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public JsBridge(MainActivity activity, SunmiPrinterHelper printerHelper) {
        this.activity = activity;
        this.printerHelper = printerHelper;
    }

    /**
     * Called from JS: window.SunmiBridge.printReceipt(text)
     */
    @JavascriptInterface
    public void printReceipt(String text) {
        Log.d(TAG, "printReceipt called, length=" + (text != null ? text.length() : 0));
        if (text == null || text.trim().isEmpty()) {
            showToast("Print text empty");
            return;
        }
        mainHandler.post(() -> {
            if (printerHelper.isReady()) {
                printerHelper.printReceipt(text);
                showToast("প্রিন্ট পাঠানো হয়েছে ✓");
                activity.updateStatus("Print sent to SUNMI");
            } else {
                showToast("প্রিন্টার রেডি নয়");
                activity.updateStatus("Printer not ready");
            }
        });
    }

    /**
     * Called from JS: window.lee.funAndroid(text)
     * (old Sunmi style bridge)
     */
    @JavascriptInterface
    public void funAndroid(String text) {
        Log.d(TAG, "funAndroid called, length=" + (text != null ? text.length() : 0));
        printReceipt(text);
    }

    /**
     * Optional: simple test print from JS
     */
    @JavascriptInterface
    public void testPrint() {
        mainHandler.post(() -> {
            if (printerHelper.isReady()) {
                printerHelper.printSelfCheck();
                showToast("Self check sent");
            } else {
                showToast("Printer not ready");
            }
        });
    }

    /**
     * Check if printer is ready (JS can call)
     */
    @JavascriptInterface
    public boolean isPrinterReady() {
        return printerHelper.isReady();
    }

    private void showToast(String msg) {
        mainHandler.post(() ->
                Toast.makeText(activity.getApplicationContext(), msg, Toast.LENGTH_SHORT).show()
        );
    }
}
