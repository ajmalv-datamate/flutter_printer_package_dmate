package com.dmate.printer_package;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.epson.epos2.printer.Printer;
import com.epson.epos2.Epos2Exception;
import com.sunmi.peripheral.printer.InnerPrinterCallback;
import com.sunmi.peripheral.printer.InnerPrinterException;
import com.sunmi.peripheral.printer.InnerPrinterManager;
import com.sunmi.peripheral.printer.SunmiPrinterService;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import print.Print;

public class FlutterPrinterPackageDmatePlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {

    private MethodChannel channel;
    private Context context;
    private Activity activity;

    // TVS printer specific variables
    private static final String ACTION_USB_PERMISSION = "com.dmate.printer_package.USB_PERMISSION";
    private PendingIntent mPermissionIntent;
    private UsbManager mUsbManager;
    private UsbDevice tvsUsbDevice;
    private Bitmap pendingPrintBitmap;
    private Result pendingResult;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_printer_package_dmate");
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.getApplicationContext();
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("getUSBDevices")) {
            result.success(getUsbDevices());
        } else if (call.method.equals("getBluetoothDevices")) {
            result.success(getBluetoothDevices());
        } else if (call.method.equals("printReceipt")) {
            handlePrintReceipt(call, result);
        } else {
            result.notImplemented();
        }
    }

    private List<String> getUsbDevices() {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        List<String> devices = new ArrayList<>();
        for (UsbDevice device : deviceList.values()) {
            devices.add(device.getDeviceName());
        }
        return devices;
    }

    private List<Map<String, String>> getBluetoothDevices() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        List<Map<String, String>> devices = new ArrayList<>();
        if (bluetoothAdapter != null) {
            @SuppressLint("MissingPermission")
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                Map<String, String> map = new HashMap<>();
                @SuppressLint("MissingPermission")
                String name = device.getName();
                String address = device.getAddress();
                map.put("name", name != null ? name : "Unknown");
                map.put("address", address);
                devices.add(map);
            }
        }
        return devices;
    }

    private void handlePrintReceipt(MethodCall call, Result result) {
        String deviceModel = call.argument("deviceModel");
        String connectionType = call.argument("connectionType");
        String connectionPath = call.argument("connectionPath");
        Map<String, Object> printerDataMap = call.argument("printerData");
        Map<String, Object> options = call.argument("options");

        if (options != null && options.containsKey("paperWidth")) {
            try {
                ReceiptGenerator.paperWidth = ((Number) options.get("paperWidth")).intValue();
            } catch (Exception e) {
                // ignore
            }
        }

        PrinterData printerData = parsePrinterData(printerDataMap);

        if (deviceModel == null) {
            result.error("INVALID_MODEL", "deviceModel cannot be null", null);
            return;
        }

        boolean isUsb = "USB".equalsIgnoreCase(connectionType);

        if (deviceModel.equalsIgnoreCase("TVS")) {
            printTVS(printerData, result);
        } else if (deviceModel.equalsIgnoreCase("BPOS") || deviceModel.equalsIgnoreCase("BPOS_POS")) {
            printBPOS(printerData, isUsb, result);
        } else if (deviceModel.equalsIgnoreCase("BPOS_NYX")) {
            printBPOSNyx(printerData, result);
        } else if (deviceModel.equalsIgnoreCase("POSIQ")) {
            printPOSIQ(printerData, result);
        } else if (deviceModel.equalsIgnoreCase("IMIN") || deviceModel.equalsIgnoreCase("IMIN_POS")) {
            printImin(printerData, result);
        } else if (deviceModel.equalsIgnoreCase("PINE_LAB")) {
            printPineLab(printerData, options, result);
        } else if (deviceModel.equalsIgnoreCase("TSUNMI") || deviceModel.equalsIgnoreCase("TSUNMI_EXTERNAL") || deviceModel.equalsIgnoreCase("TSUNMI_TAB")) {
            printSunmi(printerData, result);
        } else if (deviceModel.equalsIgnoreCase("EPSON")) {
            printEpson(printerData, connectionPath, result);
        } else {
            result.error("UNSUPPORTED_MODEL", "Printer model " + deviceModel + " is not supported", null);
        }
    }

    private void printTVS(PrinterData printerData, Result result) {
        Bitmap bitmap = ReceiptGenerator.generateReceiptForPinelab(printerData, context);
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        tvsUsbDevice = null;
        
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            int count = device.getInterfaceCount();
            for (int i = 0; i < count; i++) {
                UsbInterface intf = device.getInterface(i);
                if (intf.getInterfaceClass() == 7) {
                    tvsUsbDevice = device;
                    break;
                }
            }
        }

        if (tvsUsbDevice == null) {
            result.error("NO_PRINTER", "TVS USB printer device not found", null);
            return;
        }

        pendingPrintBitmap = bitmap;
        pendingResult = result;

        if (mUsbManager.hasPermission(tvsUsbDevice)) {
            triggerTVSPrintJob();
        } else {
            int flags = PendingIntent.FLAG_MUTABLE;
            mPermissionIntent = PendingIntent.getBroadcast(activity, 0, new Intent(ACTION_USB_PERMISSION), flags);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            activity.registerReceiver(mUsbReceiver, filter);
            mUsbManager.requestPermission(tvsUsbDevice, mPermissionIntent);
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            triggerTVSPrintJob();
                        }
                    } else {
                        if (pendingResult != null) {
                            pendingResult.error("PERMISSION_DENIED", "USB Permission Denied", null);
                            pendingResult = null;
                        }
                    }
                }
            }
        }
    };

    private void triggerTVSPrintJob() {
        try {
            if (Print.PortOpen(activity, tvsUsbDevice) == 0) {
                Print.Initialize();
                Print.PrintBitmap(pendingPrintBitmap, 1, 0);
                Print.CutPaper(1, 0);
                Print.PortClose();
                if (pendingResult != null) {
                    pendingResult.success(true);
                    pendingResult = null;
                }
            } else {
                if (pendingResult != null) {
                    pendingResult.error("CONNECT_ERROR", "TVS USB Connect Error", null);
                    pendingResult = null;
                }
            }
        } catch (Exception e) {
            if (pendingResult != null) {
                pendingResult.error("PRINT_FAILED", e.getMessage(), null);
                pendingResult = null;
            }
        }
    }

    private void printBPOS(PrinterData printerData, boolean isUsb, Result result) {
        String receiptText = ReceiptGenerator.generateReceipt(printerData);
        new BPOSDeviceIntegrationText(activity, receiptText, printerData.getLocationName(), isUsb, new BPOSDeviceResponse() {
            @Override
            public void deviceResponse(boolean printCompleted) {
                new Handler(Looper.getMainLooper()).post(() -> result.success(printCompleted));
            }
        }).bindService();
    }

    private void printBPOSNyx(PrinterData printerData, Result result) {
        if (printerData.getRawText() != null && !printerData.getRawText().trim().isEmpty()) {
            new BPOSNewDeviceIntegration(activity, printerData.getRawText(), new BPOSDeviceResponse() {
                @Override
                public void deviceResponse(boolean printCompleted) {
                    new Handler(Looper.getMainLooper()).post(() -> result.success(printCompleted));
                }
            }).bindService();
        } else {
            Bitmap bitmap = ReceiptGenerator.generateReceiptForPinelab(printerData, context);
            new BPOSNewDeviceIntegration(activity, bitmap, new BPOSDeviceResponse() {
                @Override
                public void deviceResponse(boolean printCompleted) {
                    new Handler(Looper.getMainLooper()).post(() -> result.success(printCompleted));
                }
            }).bindService();
        }
    }

    private void printPOSIQ(PrinterData printerData, Result result) {
        String receiptText = ReceiptGenerator.generateReceipt(printerData);
        new POSIQDeviceIntegration(activity, new BPOSDeviceResponse() {
            @Override
            public void deviceResponse(boolean printCompleted) {
                new Handler(Looper.getMainLooper()).post(() -> result.success(printCompleted));
            }
        }, receiptText, null).bindService();
    }

    private void printImin(PrinterData printerData, Result result) {
        String receiptText = ReceiptGenerator.generateReceipt(printerData);
        new IminPrinterManager(activity, new IminPrinterManager.IMinPrintCallback() {
            @Override
            public void onSuccess() {
                new Handler(Looper.getMainLooper()).post(() -> result.success(true));
            }

            @Override
            public void onFailure(String error) {
                new Handler(Looper.getMainLooper()).post(() -> result.error("IMIN_PRINT_ERROR", error, null));
            }
        }).printTextReceipt(receiptText);
    }

    private void printPineLab(PrinterData printerData, Map<String, Object> options, Result result) {
        Bitmap bitmap = ReceiptGenerator.generateReceiptForPinelab(printerData, context);
        String appId = "";
        if (options != null && options.containsKey("pineLabAppId")) {
            appId = String.valueOf(options.get("pineLabAppId"));
        }
        new PineLabDeviceIntegration(activity, "print", "", appId, bitmap, new PineLabDeviceResponse() {
            @Override
            public void deviceResponse(JSONObject response) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (response.optString("ResponseMsg").toLowerCase().contains("success")) {
                        result.success(true);
                    } else {
                        result.error("PINELAB_ERROR", response.optString("ResponseMsg"), null);
                    }
                });
            }

            @Override
            public void paymentDetails(JSONObject paymentDetails) {}
        }).initialise(activity);
    }

    private void printSunmi(PrinterData printerData, Result result) {
        Bitmap bitmap = ReceiptGenerator.generateReceiptForPinelab(printerData, context);
        try {
            InnerPrinterManager.getInstance().bindService(activity, new InnerPrinterCallback() {
                @Override
                protected void onConnected(SunmiPrinterService service) {
                    try {
                        service.printBitmap(bitmap, null);
                        service.printText("\n\n\n", null);
                        service.cutPaper(null);
                        InnerPrinterManager.getInstance().unBindService(activity, this);
                        new Handler(Looper.getMainLooper()).post(() -> result.success(true));
                    } catch (Exception e) {
                        new Handler(Looper.getMainLooper()).post(() -> result.error("SUNMI_ERROR", e.getMessage(), null));
                    }
                }

                @Override
                protected void onDisconnected() {
                    new Handler(Looper.getMainLooper()).post(() -> result.error("SUNMI_DISCONNECTED", "Printer service disconnected", null));
                }
            });
        } catch (Exception e) {
            result.error("SUNMI_BIND_ERROR", e.getMessage(), null);
        }
    }

    private void printEpson(PrinterData printerData, String target, Result result) {
        Bitmap bitmap = ReceiptGenerator.generateReceiptForPinelab(printerData, context);
        new Thread(() -> {
            Printer printer = null;
            try {
                printer = new Printer(Printer.TM_T88, Printer.MODEL_ANK, context);
                printer.connect(target != null ? target : "USB:", Printer.PARAM_DEFAULT);
                printer.addImage(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                        Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT,
                        Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT);
                printer.addCut(Printer.CUT_FEED);
                printer.sendData(Printer.PARAM_DEFAULT);
                printer.clearCommandBuffer();
                printer.disconnect();
                new Handler(Looper.getMainLooper()).post(() -> result.success(true));
            } catch (Epos2Exception e) {
                if (printer != null) {
                    printer.clearCommandBuffer();
                    try {
                        printer.disconnect();
                    } catch (Exception ex) {}
                }
                new Handler(Looper.getMainLooper()).post(() -> result.error("EPSON_ERROR", "Epos error code: " + e.getErrorStatus(), null));
            }
        }).start();
    }

    private PrinterData parsePrinterData(Map<String, Object> map) {
        PrinterData data = new PrinterData();
        if (map == null) return data;

        data.setClientname(getString(map, "clientname"));
        data.setRawText(getString(map, "rawText"));
        data.setLocationName(getString(map, "locationName"));
        data.setLocationName1(getString(map, "locationName1"));
        data.setLocationName2(getString(map, "locationName2"));
        data.setBillNo(getString(map, "billNo"));
        data.setDate(getString(map, "date"));
        data.setTotal(getString(map, "total"));
        data.setSgst(getString(map, "sgst"));
        data.setCgst(getString(map, "cgst"));
        data.setTotalPayable(getString(map, "totalPayable"));
        data.setAmountInWords(getString(map, "amountInWords"));
        data.setPaymentMode(getString(map, "paymentMode"));
        data.setBilledFor(getString(map, "billedFor"));
        data.setOutlet(getString(map, "outlet"));
        data.setGstIN(getString(map, "gstIN"));
        data.setFssai(getString(map, "fssai"));
        data.setTable(getString(map, "table"));
        data.setWaiter(getString(map, "waiter"));
        data.setRemarks(getString(map, "remarks"));
        data.setParcel(getString(map, "parcel"));
        data.setSession(getString(map, "session"));
        data.setSgstData(getString(map, "sgstData"));
        data.setSgstPer(getString(map, "sgstPer"));
        data.setCgstData(getString(map, "cgstData"));
        data.setCgstPer(getString(map, "cgstPer"));
        data.setCess(getString(map, "cess"));
        data.setCessPer(getString(map, "cessPer"));
        data.setPax(getString(map, "pax"));
        data.setKot(getString(map, "kot"));
        data.setEmail(getString(map, "email"));
        data.setPhone(getString(map, "phone"));
        data.setCompliEmp(getString(map, "compliEmp"));
        data.setCompliAuth(getString(map, "compliAuth"));
        data.setCompliCompany(getString(map, "compliCompany"));

        if (map.containsKey("showGSTSplitting")) {
            Object val = map.get("showGSTSplitting");
            if (val instanceof Boolean) {
                data.setShowGSTSplitting((Boolean) val);
            } else if (val != null) {
                data.setShowGSTSplitting(Boolean.parseBoolean(String.valueOf(val)));
            }
        }

        List<Map<String, Object>> cartItemsList = (List<Map<String, Object>>) map.get("cartItems");
        List<PrinterData.CartItem> cartItems = new ArrayList<>();
        if (cartItemsList != null) {
            for (Map<String, Object> itemMap : cartItemsList) {
                PrinterData.CartItem item = new PrinterData.CartItem();
                item.setItemName(getString(itemMap, "itemName"));
                item.setQuantity(getString(itemMap, "quantity"));
                item.setPrice(getString(itemMap, "price"));
                item.setTotal(getString(itemMap, "total"));
                cartItems.add(item);
            }
        }
        data.setCartItems(cartItems);
        return data;
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return "";
        return String.valueOf(val);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        activity = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
    }
}
