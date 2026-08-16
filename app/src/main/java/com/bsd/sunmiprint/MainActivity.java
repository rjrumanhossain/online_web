package com.bsd.sunmiprint;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String PREFS = "sunmi_prefs";
    private static final String KEY_URL = "home_url";

    // >>> এখানে আপনার ওয়েবসাইটের মূল URL দিন <<<
    private static final String DEFAULT_HOME_URL = "https://your-domain.com/";

    private WebView webView;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private TextView tvTitle;
    private ImageButton btnBack, btnHome, btnRefresh, btnSettings;

    private SunmiPrinterHelper printerHelper;
    private JsBridge jsBridge;
    private SharedPreferences prefs;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        initViews();
        initPrinter();
        setupWebView();
        setupButtons();

        // Load home URL
        String url = prefs.getString(KEY_URL, DEFAULT_HOME_URL);
        webView.loadUrl(url);
    }

    private void initViews() {
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
        tvTitle = findViewById(R.id.tvTitle);
        btnBack = findViewById(R.id.btnBack);
        btnHome = findViewById(R.id.btnHome);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnSettings = findViewById(R.id.btnSettings);
    }

    private void initPrinter() {
        printerHelper = new SunmiPrinterHelper(this);
        printerHelper.setOnPrinterReadyListener(success -> {
            runOnUiThread(() -> {
                if (success) {
                    updateStatus("SUNMI Printer Ready ✓");
                    Log.d(TAG, "Printer ready");
                } else {
                    updateStatus("Printer service not found (not a Sunmi device?)");
                    Log.w(TAG, "Printer not ready");
                }
            });
        });
        printerHelper.bindService();

        jsBridge = new JsBridge(this, printerHelper);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setMediaPlaybackRequiresUserGesture(false);

        // Important: allow cleartext if your site is http
        // (already set in Manifest usesCleartextTraffic=true)

        // Inject both bridge names so your PHP works as-is
        webView.addJavascriptInterface(jsBridge, "SunmiBridge");
        webView.addJavascriptInterface(jsBridge, "lee");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(0);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                tvTitle.setText(view.getTitle() != null ? view.getTitle() : "BSD Sunmi Print");

                // Re-inject bridge safety (some pages recreate window)
                injectBridgeHelper();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    updateStatus("Load error: " + error.getDescription());
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false; // load inside WebView
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress >= 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * Extra safety: make sure window.SunmiBridge and window.lee exist
     */
    private void injectBridgeHelper() {
        String js = "javascript:(function(){"
                + "if(typeof window.SunmiBridge==='undefined'){window.SunmiBridge={};}"
                + "if(typeof window.lee==='undefined'){window.lee={};}"
                + "console.log('Sunmi bridge injected');"
                + "})();";
        webView.evaluateJavascript(js, null);
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                Toast.makeText(this, "আর পেছনে যাওয়া যাবে না", Toast.LENGTH_SHORT).show();
            }
        });

        btnHome.setOnClickListener(v -> {
            String url = prefs.getString(KEY_URL, DEFAULT_HOME_URL);
            webView.loadUrl(url);
        });

        btnRefresh.setOnClickListener(v -> webView.reload());

        btnSettings.setOnClickListener(v -> showSettingsDialog());
    }

    private void showSettingsDialog() {
        EditText input = new EditText(this);
        input.setHint("https://your-domain.com/");
        input.setSingleLine(true);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setText(prefs.getString(KEY_URL, DEFAULT_HOME_URL));
        input.setPadding(40, 30, 40, 30);

        new AlertDialog.Builder(this)
                .setTitle("Home URL সেট করুন")
                .setMessage("আপনার PHP সফটওয়্যারের মূল লিংক দিন")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String url = input.getText().toString().trim();
                    if (!url.isEmpty()) {
                        if (!url.startsWith("http://") && !url.startsWith("https://")) {
                            url = "https://" + url;
                        }
                        prefs.edit().putString(KEY_URL, url).apply();
                        webView.loadUrl(url);
                        Toast.makeText(this, "URL সেভ হয়েছে", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Test Print", (d, w) -> {
                    if (printerHelper.isReady()) {
                        printerHelper.printSelfCheck();
                        Toast.makeText(this, "Test print পাঠানো হয়েছে", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Printer ready নয়", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void updateStatus(String msg) {
        runOnUiThread(() -> {
            tvStatus.setText(msg);
            tvStatus.setVisibility(View.VISIBLE);
            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(() -> tvStatus.setVisibility(View.GONE), 3000);
        });
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (printerHelper != null) {
            printerHelper.unbindService();
        }
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
