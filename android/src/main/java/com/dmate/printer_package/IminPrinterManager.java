package com.dmate.printer_package;

import android.app.Activity;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

import com.imin.printer.INeoPrinterCallback;
import com.imin.printer.PrinterHelper;

public class IminPrinterManager {

    private static final String TAG = "IMinPrinter";

    private Activity context;
    private IMinPrintCallback callback;

    public interface IMinPrintCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public IminPrinterManager(Activity context, IMinPrintCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void printTextReceipt(String receiptData) {
        PrinterHelper.getInstance().initPrinterService(context);

        PrinterHelper.getInstance().initPrinter(context.getPackageName(), new INeoPrinterCallback() {
            @Override
            public void onRunResult(boolean success) throws RemoteException {
                Log.d(TAG, "Init: " + success);
                if (success) {
                    new Handler().postDelayed(() -> startPrint(receiptData), 300);
                } else {
                    if (callback != null) callback.onFailure("Printer init failed");
                }
            }

            @Override
            public void onReturnString(String result) throws RemoteException {
                Log.d(TAG, "Return: " + result);
            }

            @Override
            public void onRaiseException(int code, String msg) throws RemoteException {
                Log.e(TAG, "Exception: " + msg);
                if (callback != null) callback.onFailure(msg);
            }

            @Override
            public void onPrintResult(int code, String msg) throws RemoteException {
                Log.d(TAG, "Init Print Result: " + msg);
            }
        });
    }

    private void startPrint(String receiptData) {
        try {
            PrinterHelper.getInstance().printAndFeedPaper(2);
            PrinterHelper.getInstance().printText(receiptData, new INeoPrinterCallback() {
                @Override
                public void onRunResult(boolean success) throws RemoteException {}

                @Override
                public void onReturnString(String result) throws RemoteException {}

                @Override
                public void onRaiseException(int code, String msg) throws RemoteException {
                    if (callback != null) callback.onFailure(msg);
                }

                @Override
                public void onPrintResult(int code, String msg) throws RemoteException {
                    if (callback != null) callback.onSuccess();
                }
            });
            PrinterHelper.getInstance().printAndLineFeed();
        } catch (Exception e) {
            if (callback != null) callback.onFailure(e.getMessage());
        }
    }
}
