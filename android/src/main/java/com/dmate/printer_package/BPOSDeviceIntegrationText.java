package com.dmate.printer_package;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import net.posprinter.IDeviceConnection;
import net.posprinter.IPOSListener;
import net.posprinter.POSConnect;
import net.posprinter.POSConst;
import net.posprinter.POSPrinter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BPOSDeviceIntegrationText {

    Activity context;
    String receipt;
    String compName;
    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1001;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SPP UUID
    private BluetoothAdapter bluetoothAdapter;
    private String devicesAddress;
    private List<String> pairedDeviceNames;
    private List<String> pairedDeviceAddresses;
    private boolean isUSBDevice = false;
    public static IDeviceConnection bposDeviceCon;
    private BPOSDeviceResponse deviceResponse;

    public BPOSDeviceIntegrationText(Activity context, String receipt, String compName, boolean isUSBDevice, BPOSDeviceResponse bposDeviceResponse) {
        this.context = context;
        this.receipt = receipt;
        this.compName = compName;
        this.isUSBDevice = isUSBDevice;
        this.deviceResponse = bposDeviceResponse;
    }

    public void bindService() {
        if (isUSBDevice){
            initialiseUSBBPOSPrinter();
        }else {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            pairedDeviceNames = new ArrayList<>();
            pairedDeviceAddresses = new ArrayList<>();
            if (bluetoothAdapter == null) {
                return;
            }

            if (!checkPermissions()) {
                requestPermissions();
            } else {
                setupBluetooth();
            }
        }
    }

    void initialiseUSBBPOSPrinter(){
        try {
            POSConnect.init(context);
            List<String> entries = POSConnect.getUsbDevices(context);

            if (!entries.isEmpty()) {
                try {
                    devicesAddress = entries.get(0);
                    POSConnect.init(context);
                    connectBPOSUSB(entries.get(0));
                } catch (Exception e) {
                    Toast.makeText(context, "Error printing receipt: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(context, "No USB printer devices found", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error printing receipt: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    void connectBPOSUSB(String path){
        if (bposDeviceCon!=null){
            bposDeviceCon.close();
        }
        bposDeviceCon = POSConnect.createDevice(POSConnect.DEVICE_TYPE_USB);
        bposDeviceCon.connect(path, new IPOSListener() {
            @Override
            public void onStatus(int i, String s) {
                switch (i) {
                    case POSConnect.CONNECT_SUCCESS:
                        Toast.makeText(context, "Connection Success", Toast.LENGTH_SHORT).show();
                        printUsbDeviceReciept(receipt);
                        break;
                    case POSConnect.CONNECT_FAIL:
                        Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show();
                        break;
                    case POSConnect.CONNECT_INTERRUPT:
                        Toast.makeText(context, "Connection Interrupt", Toast.LENGTH_SHORT).show();
                        break;
                    case POSConnect.SEND_FAIL:
                        Toast.makeText(context, "Send Fail", Toast.LENGTH_SHORT).show();
                        break;
                    case POSConnect.USB_DETACHED:
                        Toast.makeText(context, "USB Detached", Toast.LENGTH_SHORT).show();
                        break;
                    case POSConnect.USB_ATTACHED:
                        Toast.makeText(context, "USB Attached", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + s);
                }
            }
        });
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(context,
                    new String[] {
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    REQUEST_BLUETOOTH_PERMISSIONS);
        } else {
            ActivityCompat.requestPermissions(context,
                    new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    REQUEST_BLUETOOTH_PERMISSIONS);
        }
    }

    @SuppressLint("MissingPermission")
    private void setupBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.isEmpty()) {
            Toast.makeText(context, "No paired Bluetooth devices found", Toast.LENGTH_SHORT).show();
        } else {
            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceNames.add(device.getName());
                pairedDeviceAddresses.add(device.getAddress());
            }
            if (!pairedDeviceAddresses.isEmpty()){
                devicesAddress = pairedDeviceAddresses.get(0);
                Toast.makeText(context, "Selected device: " + pairedDeviceNames.get(0), Toast.LENGTH_SHORT).show();
                printUsbDeviceReciept(receipt);
            }else {
                Toast.makeText(context, "Device Connection Error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void printUsbDeviceReciept(String receipt){
        POSPrinter printer = new POSPrinter(bposDeviceCon);
        String companyName = wrapTwoLines(
                compName != null ? compName : "Receipt",
                24
        );
        printer.printText(
                companyName+"\n",
                POSConst.ALIGNMENT_CENTER,
                POSConst.FNT_BOLD,
                14
        );
        safePrintText(printer, receipt);
        printer.cutHalfAndFeed(1);
        deviceResponse.deviceResponse(true);
    }

    private String wrapTwoLines(String text, int maxCharsPerLine) {
        String[] words = text.split(" ");
        StringBuilder line1 = new StringBuilder();
        StringBuilder line2 = new StringBuilder();

        for (String word : words) {
            if ((line1.length() + word.length() + 1) <= maxCharsPerLine) {
                if (line1.length() > 0) {
                    line1.append(" ");
                }
                line1.append(word);
            } else {
                if ((line2.length() + word.length() + 1) <= maxCharsPerLine) {
                    if (line2.length() > 0) {
                        line2.append(" ");
                    }
                    line2.append(word);
                } else {
                    break;
                }
            }
        }
        return line1.toString() + "\n" + line2.toString();
    }

    @SuppressLint("MissingPermission")
    private void printReceipt(Bitmap bitmap) {
        if (devicesAddress == null) {
            Toast.makeText(context, "No Bluetooth device selected", Toast.LENGTH_SHORT).show();
            return;
        }

        BluetoothDevice printerDevice = bluetoothAdapter.getRemoteDevice(devicesAddress);

        try (BluetoothSocket socket = printerDevice.createRfcommSocketToServiceRecord(MY_UUID)) {
            socket.connect();
            OutputStream outputStream = socket.getOutputStream();
            byte[] bitmapData = bitmapToESCBitmapBytes(bitmap);
            outputStream.write(bitmapData);
            outputStream.write("\n\n\n".getBytes());
            Toast.makeText(context, "Printed successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, "Printing failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void safePrintText(POSPrinter printer, String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        try {
            text = text.replace("\r\n", "\n");
            if (text.length() > 256) {
                printTextInChunks(printer, text);
                return;
            }
            printer.setCharRightSpace((byte) 1);
            printer.printText(
                    text,
                    POSConst.ALIGNMENT_LEFT,
                    POSConst.STS_NORMAL,
                    POSConst.TXT_1WIDTH
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printTextInChunks(POSPrinter printer, String text) {
        final int CHUNK_SIZE = 256;
        int length = text.length();

        for (int i = 0; i < length; i += CHUNK_SIZE) {
            int end = Math.min(i + CHUNK_SIZE, length);
            String chunk = text.substring(i, end);

            try {
                byte[] data = chunk.getBytes("UTF-8");
                printer.printText(new String(data, "UTF-8"),
                        POSConst.ALIGNMENT_LEFT,
                        POSConst.STS_NORMAL,
                        POSConst.TXT_1WIDTH);
            } catch (Exception e) {
                try {
                    byte[] data = chunk.getBytes("GBK");
                    printer.printText(new String(data, "GBK"),
                            POSConst.ALIGNMENT_LEFT,
                            POSConst.STS_NORMAL,
                            POSConst.TXT_1WIDTH);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private byte[] bitmapToESCBitmapBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        stream.write(0x1B);
        stream.write(0x33);
        stream.write(24);

        for (int y = 0; y < height; y += 24) {
            stream.write(0x1B);
            stream.write('*');
            stream.write(33);
            stream.write(width & 0xFF);
            stream.write((width >> 8) & 0xFF);

            for (int x = 0; x < width; x++) {
                for (int k = 0; k < 3; k++) {
                    byte slice = 0;
                    for (int b = 0; b < 8; b++) {
                        int yPosition = y + k * 8 + b;
                        if (yPosition >= height) continue;

                        int pixel = bitmap.getPixel(x, yPosition);
                        int r = (pixel >> 16) & 0xff;
                        int g = (pixel >> 8) & 0xff;
                        int bVal = pixel & 0xff;
                        int gray = (r + g + bVal) / 3;
                        if (gray < 128) {
                            slice |= (1 << (7 - b));
                        }
                    }
                    stream.write(slice);
                }
            }
            stream.write(0x0A);
        }
        return stream.toByteArray();
    }
}
