package com.dmate.printer_package;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.printer.sdk.command.EscCommand;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class POSIQDeviceIntegration {

    Activity context;
    private UsbManager usbManager;
    private static final int CONN_STATE_DISCONN = 0x007;
    private static int USBCONNECT_ENABLE = 1;
    private BPOSDeviceResponse deviceResponse;
    private PendingIntent mPermissionIntent;
    public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    static private int id = 0;

    private Handler mHandler;
    private String receiptText;
    private Bitmap receiptBitmap;

    public POSIQDeviceIntegration(Activity context, BPOSDeviceResponse bposDeviceResponse, String receiptText, Bitmap receiptBitmap) {
        this.context = context;
        this.deviceResponse = bposDeviceResponse;
        this.receiptText = receiptText;
        this.receiptBitmap = receiptBitmap;
    }

    public void bindService() {
        mHandler = new Handler(context.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == CONN_STATE_DISCONN) {
                    if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] != null) {
                        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].closePort(id);
                    }
                    if (deviceResponse != null) {
                        deviceResponse.deviceResponse(true);
                    }
                }
            }
        };

        initialiseUSBBPrinter();
    }

    void initialiseUSBBPrinter() {
        try {
            if (USBCONNECT_ENABLE == 1) {
                usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
                HashMap<String, UsbDevice> devices = usbManager.getDeviceList();
                Iterator<UsbDevice> iterator = devices.values().iterator();

                if (devices.size() > 0) {
                    while (iterator.hasNext()) {
                        UsbDevice device = iterator.next();
                        if (checkUsbDevicePidVid(device)) {
                            if (usbManager.hasPermission(device)) {
                                usbConn(device);
                            } else {
                                int flags = PendingIntent.FLAG_MUTABLE;
                                mPermissionIntent = PendingIntent.getBroadcast(
                                        context, 0,
                                        new Intent(ACTION_USB_PERMISSION), flags);
                                usbManager.requestPermission(device, mPermissionIntent);
                            }

                            new Thread(() -> {
                                try {
                                    if (receiptText != null && !receiptText.trim().isEmpty()) {
                                        sendTextReceipt(receiptText);
                                    } else if (receiptBitmap != null) {
                                        sendBitmapReceipt(receiptBitmap);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                USBCONNECT_ENABLE = 0;
                                mHandler.obtainMessage(CONN_STATE_DISCONN).sendToTarget();
                                USBCONNECT_ENABLE = 1;
                            }).start();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error printing receipt: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public boolean checkUsbDevicePidVid(UsbDevice dev) {
        int pid = dev.getProductId();
        int vid = dev.getVendorId();
        return ((vid == 34918 && pid == 256) || (vid == 1137 && pid == 85));
    }

    private void usbConn(UsbDevice usbDevice) {
        new DeviceConnFactoryManager.Build()
                .setId(id)
                .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.USB)
                .setUsbDevice(usbDevice)
                .setContext(context)
                .build();
        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
    }

    public void sendTextReceipt(String text) {
        try {
            EscCommand esc = new EscCommand();
            esc.addInitializePrinter();
            esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
            esc.addPrintAndFeedLines((byte) 3);
            esc.addText(text);
            esc.addPrintAndFeedLines((byte) 3);
            esc.addCutAndFeedPaper((byte) 0x00);

            Vector<Byte> datas = esc.getCommand();
            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(datas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendBitmapReceipt(Bitmap bitmap) {
        try {
            EscCommand esc = new EscCommand();
            esc.addInitializePrinter();
            esc.addPrintAndFeedLines((byte) 3);
            esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
            esc.addRastBitImage(bitmap, bitmap.getWidth(), 0);
            esc.addPrintAndFeedLines((byte) 9);
            esc.addCutAndFeedPaper((byte) 0x0);

            Vector<Byte> datas = esc.getCommand();
            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(datas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
