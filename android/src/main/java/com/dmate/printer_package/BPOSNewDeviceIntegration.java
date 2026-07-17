package com.dmate.printer_package;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

// Import Nyx Printer Service interfaces
import net.nyx.printerservice.print.IPrinterService;
import net.nyx.printerservice.print.PrintTextFormat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BPOSNewDeviceIntegration {

    private static final String TAG = "BPOSNewDevice";
    
    private final Activity context;
    private final String receiptText;
    private final Bitmap bitmap;
    private final BPOSDeviceResponse deviceResponse;
    
    private final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    private final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
    
    private IPrinterService printerService;
    
    private final ServiceConnection connService = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            printerService = null;
            // Attempt to reconnect if Nyx service is disconnected
            handler.postDelayed(() -> bindNyxService(), 5000);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            printerService = IPrinterService.Stub.asInterface(service);
            // Once Nyx service is connected successfully, print
            printNyx();
        }
    };

    public BPOSNewDeviceIntegration(Activity context, String receiptText, BPOSDeviceResponse bposDeviceResponse) {
        this.context = context;
        this.receiptText = receiptText;
        this.bitmap = null;
        this.deviceResponse = bposDeviceResponse;
    }

    public BPOSNewDeviceIntegration(Activity context, Bitmap bitmap, BPOSDeviceResponse bposDeviceResponse) {
        this.context = context;
        this.receiptText = null;
        this.bitmap = bitmap;
        this.deviceResponse = bposDeviceResponse;
    }

    public void bindService() {
        bindNyxService();
    }

    private void bindNyxService() {
        Intent intent = new Intent();

        intent.setPackage("net.nyx.printerservice");
        intent.setAction("net.nyx.printerservice.IPrinterService");
        try {
            context.bindService(intent, connService, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Log.e(TAG, "Failed to bind Nyx service", e);
            Toast.makeText(context, "Failed to bind internal printer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void unbindService() {
        if (printerService != null) {
            try {
                context.unbindService(connService);
            } catch (Exception e) {
                Log.e(TAG, "Error unbinding Nyx service", e);
            }
            printerService = null;
        }
    }

    private void printNyx() {
        if (printerService == null) {
            Toast.makeText(context, "Nyx Printer Service not connected", Toast.LENGTH_SHORT).show();
            if (deviceResponse != null) {
                deviceResponse.deviceResponse(false);
            }
            return;
        }

        singleThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    int ret;
                    if (bitmap != null) {
                        ret = printerService.printBitmap(bitmap, 0, 1);
                    } else if (receiptText != null) {
                        PrintTextFormat textFormat = new PrintTextFormat();
                        textFormat.setTextSize(24); // default size (24px)
                        ret = printerService.printText(receiptText, textFormat);
                    } else {
                        ret = -1;
                    }

                    if (ret == 0) {
                        printerService.printEndAutoOut();
                        context.runOnUiThread(() -> Toast.makeText(context, "Printed successfully", Toast.LENGTH_SHORT).show());
                        if (deviceResponse != null) {
                            deviceResponse.deviceResponse(true);
                        }
                    } else {
                        context.runOnUiThread(() -> Toast.makeText(context, "Printing failed with status: " + ret, Toast.LENGTH_SHORT).show());
                        if (deviceResponse != null) {
                            deviceResponse.deviceResponse(false);
                        }
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Nyx print error", e);
                    context.runOnUiThread(() -> Toast.makeText(context, "Printing failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    if (deviceResponse != null) {
                        deviceResponse.deviceResponse(false);
                    }
                }
            }
        });
    }
}
