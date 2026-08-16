package com.bsd.sunmiprint;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.sunmi.peripheral.printer.InnerPrinterCallback;
import com.sunmi.peripheral.printer.InnerPrinterManager;
import com.sunmi.peripheral.printer.SunmiPrinterService;

/**
 * Helper class for Sunmi built-in thermal printer (V2 / V2s / V2s Plus etc.)
 * Uses official Sunmi printerlibrary.
 */
public class SunmiPrinterHelper {

    private static final String TAG = "SunmiPrinterHelper";

    private Context context;
    private SunmiPrinterService printerService;
    private boolean isBound = false;
    private OnPrinterReadyListener readyListener;

    public interface OnPrinterReadyListener {
        void onReady(boolean success);
    }

    public SunmiPrinterHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public void setOnPrinterReadyListener(OnPrinterReadyListener listener) {
        this.readyListener = listener;
    }

    /**
     * Bind to Sunmi inner printer service
     */
    public void bindService() {
        try {
            boolean result = InnerPrinterManager.getInstance().bindService(context, innerPrinterCallback);
            Log.d(TAG, "bindService result: " + result);
            if (!result && readyListener != null) {
                readyListener.onReady(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "bindService error", e);
            if (readyListener != null) {
                readyListener.onReady(false);
            }
        }
    }

    private final InnerPrinterCallback innerPrinterCallback = new InnerPrinterCallback() {
        @Override
        protected void onConnected(SunmiPrinterService service) {
            printerService = service;
            isBound = true;
            Log.d(TAG, "Printer service connected");
            if (readyListener != null) {
                readyListener.onReady(true);
            }
        }

        @Override
        protected void onDisconnected() {
            printerService = null;
            isBound = false;
            Log.d(TAG, "Printer service disconnected");
        }
    };

    public boolean isReady() {
        return isBound && printerService != null;
    }

    /**
     * Print plain text receipt (line by line).
     * Text should contain \n for new lines.
     */
    public void printText(String text) {
        if (!isReady()) {
            Log.e(TAG, "Printer not ready");
            return;
        }
        try {
            printerService.printerInit(null);
            printerService.setAlignment(0, null); // left
            printerService.setFontSize(24, null);

            // Split by lines and print
            String[] lines = text.split("\\n");
            for (String line : lines) {
                // Keep empty lines for spacing
                printerService.printText(line + "\n", null);
            }

            // Feed a few lines so paper can be torn
            printerService.lineWrap(3, null);

            // Cut paper if cutter available (most V2s have no cutter, but safe to call)
            try {
                printerService.cutPaper(null);
            } catch (Exception ignored) {
            }

            Log.d(TAG, "Print job sent, length=" + text.length());
        } catch (RemoteException e) {
            Log.e(TAG, "printText error", e);
        }
    }

    /**
     * Print with better formatting for 58mm paper.
     */
    public void printReceipt(String text) {
        if (!isReady()) {
            Log.e(TAG, "Printer not ready");
            return;
        }
        try {
            printerService.printerInit(null);

            String[] lines = text.split("\\n");
            for (String raw : lines) {
                String line = raw != null ? raw.trim() : "";

                // Detect separator / cut line
                if (line.contains("CUT HERE") || line.contains("✂") || line.matches("^-{5,}$") || line.matches("^={5,}$")) {
                    printerService.printText("--------------------------------\n", null);
                    continue;
                }

                // Title-ish lines (short + uppercase-ish)
                if (line.equalsIgnoreCase("CLIENT COPY") || line.equalsIgnoreCase("OFFICE COPY")
                        || line.equalsIgnoreCase("INVOICE / BILL") || line.startsWith("TOTAL DUE")) {
                    printerService.setAlignment(1, null); // center
                    printerService.setFontSize(28, null);
                    printerService.printText(line + "\n", null);
                    printerService.setAlignment(0, null);
                    printerService.setFontSize(24, null);
                    continue;
                }

                // Normal line
                printerService.setAlignment(0, null);
                printerService.setFontSize(24, null);
                printerService.printText(line + "\n", null);
            }

            printerService.lineWrap(4, null);
            try {
                printerService.cutPaper(null);
            } catch (Exception ignored) {
            }

            Log.d(TAG, "Receipt print job sent");
        } catch (RemoteException e) {
            Log.e(TAG, "printReceipt error", e);
        }
    }

    /**
     * Self check / test print
     */
    public void printSelfCheck() {
        if (!isReady()) return;
        try {
            printerService.printerSelfChecking(null);
        } catch (RemoteException e) {
            Log.e(TAG, "self check error", e);
        }
    }

    public void unbindService() {
        try {
            if (isBound) {
                InnerPrinterManager.getInstance().unBindService(context, innerPrinterCallback);
                isBound = false;
                printerService = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "unbind error", e);
        }
    }
}
