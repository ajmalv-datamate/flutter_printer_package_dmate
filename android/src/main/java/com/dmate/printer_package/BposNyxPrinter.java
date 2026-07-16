package com.dmate.printer_package;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.nyx.printerservice.print.IPrinterService;
import net.nyx.printerservice.print.PrintTextFormat;

/**
 * Business logic class for handling printing tasks on the Nyx printer.
 */
public class BposNyxPrinter {

    private static final String TAG = "BposNyxPrinter";
    private static IPrinterService printerService;
    private static ServiceConnection connService;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler handler = new Handler();
    private static ConnectionListener connectionListener;
    private static Context appContext;

    private final String text;

    /**
     * Constructs a printer utility for printing the specified text.
     *
     * @param text The text to print.
     */
    public BposNyxPrinter(String text) {
        this.text = text;
    }

    /**
     * Interface for monitoring printer service connection status.
     */
    public interface ConnectionListener {
        void onConnected(IPrinterService service);
        void onDisconnected();
    }

    /**
     * Interface for print completion callbacks.
     */
    public interface PrintCallback {
        void onResult(int code);
        void onError(Exception e);
    }

    /**
     * Binds the printer service.
     *
     * @param context The application or activity context.
     * @param listener A listener to receive connection notifications.
     */
    public static void bindService(Context context, ConnectionListener listener) {
        if (context != null) {
            appContext = context.getApplicationContext();
        }
        if (printerService != null) {
            if (listener != null) {
                listener.onConnected(printerService);
            }
            return;
        }
        connectionListener = listener;
        connService = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "onServiceConnected: " + name);
                printerService = IPrinterService.Stub.asInterface(service);
                if (connectionListener != null) {
                    connectionListener.onConnected(printerService);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected: " + name);
                printerService = null;
                if (connectionListener != null) {
                    connectionListener.onDisconnected();
                }
                // Try to reconnect
                handler.postDelayed(() -> {
                    if (appContext != null) {
                        bindService(appContext, connectionListener);
                    }
                }, 5000);
            }
        };

        Intent intent = new Intent();
        intent.setPackage("net.nyx.printerservice");
        intent.setAction("net.nyx.printerservice.IPrinterService");
        context.bindService(intent, connService, Context.BIND_AUTO_CREATE);
    }

    /**
     * Unbinds the printer service.
     *
     * @param context The application or activity context.
     */
    public static void unbindService(Context context) {
        if (connService != null) {
            context.unbindService(connService);
            connService = null;
            printerService = null;
            connectionListener = null;
            appContext = null;
        }
    }

    /**
     * Retrieves the active printer service binder.
     *
     * @return The active printer service, or null if disconnected.
     */
    public static IPrinterService getPrinterService() {
        return printerService;
    }

    /**
     * Executes the print command on the printer service.
     *
     * @param callback Callback to notify of print results or errors.
     */
    public void print(final PrintCallback callback) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                IPrinterService service = getPrinterService();
                if (service == null) {
                    if (callback != null) {
                        callback.onError(new Exception("Printer service is not connected"));
                    }
                    return;
                }
                try {
                    PrintTextFormat textFormat = new PrintTextFormat();
                    int ret = service.printText(text, textFormat);
                    if (callback != null) {
                        callback.onResult(ret);
                    }
                    if (ret == 0) {
                        service.printEndAutoOut();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onError(e);
                    }
                }
            }
        });
    }
}
