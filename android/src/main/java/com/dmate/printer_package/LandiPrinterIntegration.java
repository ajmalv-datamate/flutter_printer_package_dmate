package com.dmate.printer_package;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.sdksuite.omnidriver.OmniConnection;
import com.sdksuite.omnidriver.OmniDriver;
import com.sdksuite.omnidriver.aidl.printer.Align;
import com.sdksuite.omnidriver.api.Error;
import com.sdksuite.omnidriver.api.OnPrintListener;
import com.sdksuite.omnidriver.api.Printer;
import com.sdksuite.omnidriver.api.PrinterException;

import java.io.ByteArrayOutputStream;

/**
 * Driver integration for Landi Android POS hardware using the OmniDriver SDK.
 */
public class LandiPrinterIntegration {

    private static final String TAG = "LandiPrinterIntegration";
    private static final int DEFAULT_MAX_WIDTH = 384;

    public interface LandiPrintCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    private final Context context;
    private final Bitmap bitmap;
    private final int paperWidth;
    private final LandiPrintCallback callback;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public LandiPrinterIntegration(Context context, Bitmap bitmap, int paperWidth, LandiPrintCallback callback) {
        this.context = context;
        this.bitmap = bitmap;
        this.paperWidth = paperWidth > 0 ? paperWidth : DEFAULT_MAX_WIDTH;
        this.callback = callback;
    }

    public void print() {
        if (bitmap == null) {
            postFailure("Bitmap generation failed or receipt bitmap is null");
            return;
        }

        new Thread(() -> {
            try {
                final OmniDriver omniDriver = OmniDriver.me(context);
                if (omniDriver == null) {
                    postFailure("Failed to initialize Landi OmniDriver");
                    return;
                }

                omniDriver.init(new OmniConnection() {
                    @Override
                    public void onConnected() {
                        new Thread(() -> executePrintJob(omniDriver)).start();
                    }

                    @Override
                    public void onDisconnected(int error) {
                        Log.e(TAG, "OmniDriver onDisconnected: " + error);
                        if (error == OmniDriver.ERROR_BIND_FAIL) {
                            postFailure("Landi OmniDriver service is not installed on this device. Please install the OmniDriver service.");
                        } else {
                            postFailure("Landi OmniDriver disconnected with error: " + error);
                        }
                    }
                });
            } catch (NoClassDefFoundError | Exception e) {
                Log.e(TAG, "Landi OmniDriver init error", e);
                postFailure("Landi OmniDriver not supported on this device: " + e.getMessage());
            }
        }).start();
    }

    private void executePrintJob(OmniDriver omniDriver) {
        Printer printer = null;
        try {
            printer = omniDriver.getPrinter(new Bundle());
            if (printer == null) {
                postFailure("Unable to acquire Landi Printer service");
                return;
            }

            printer.openDevice(0);
            int status = printer.getStatus();
            if (status != Error.PRINTER_NONE) {
                try { printer.closeDevice(); } catch (Exception ignored) {}
                postFailure("Landi printer not ready, status: " + getStatusDescription(status));
                return;
            }

            // Prepare bitmap
            Bitmap scaledBitmap = bitmap;
            if (scaledBitmap.getWidth() > paperWidth) {
                float scale = (float) paperWidth / scaledBitmap.getWidth();
                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);
                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            outputStream.close();

            printer.setGray(3);
            printer.addImage(imageBytes, Align.CENTER, 0);
            printer.feedLine(3);
            try {
                printer.cutPaper();
            } catch (Exception ignored) {
                // Cutter may not be present on all Landi handhelds
            }

            final Printer finalPrinter = printer;
            printer.startPrint(new OnPrintListener() {
                @Override
                public void onSuccess() {
                    try { finalPrinter.closeDevice(); } catch (Exception ignored) {}
                    postSuccess();
                }

                @Override
                public void onFail(int error) {
                    try { finalPrinter.closeDevice(); } catch (Exception ignored) {}
                    postFailure("Landi print failed with error code: " + error);
                }
            });

        } catch (PrinterException pe) {
            Log.e(TAG, "PrinterException during Landi print", pe);
            if (printer != null) {
                try { printer.closeDevice(); } catch (Exception ignored) {}
            }
            postFailure("Landi PrinterException: " + pe.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception during Landi print", e);
            if (printer != null) {
                try { printer.closeDevice(); } catch (Exception ignored) {}
            }
            postFailure("Landi print error: " + e.getMessage());
        }
    }

    private String getStatusDescription(int status) {
        switch (status) {
            case 0:
                return "OK";
            case 1:
                return "Out of paper";
            case 2:
                return "Printer overheating";
            case 3:
                return "Cover open";
            default:
                return "Status code: " + status;
        }
    }

    private void postSuccess() {
        mainHandler.post(() -> {
            if (callback != null) callback.onSuccess();
        });
    }

    private void postFailure(String msg) {
        mainHandler.post(() -> {
            if (callback != null) callback.onFailure(msg);
        });
    }
}
