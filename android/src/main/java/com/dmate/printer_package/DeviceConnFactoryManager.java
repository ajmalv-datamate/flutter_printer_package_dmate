package com.dmate.printer_package;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Log;

import com.printer.sdk.io.BluetoothPort;
import com.printer.sdk.io.PortManager;
import com.printer.sdk.io.UsbPort;

import java.io.IOException;
import java.util.Vector;

public class DeviceConnFactoryManager {
    private static final String TAG = "DeviceConnFactory";
    private PortManager mPort;
    private Context mContext;
    private UsbDevice mUsbDevice;
    private String macAddress;
    private CONN_METHOD connMethod;
    private int id;
    
    private static DeviceConnFactoryManager[] deviceConnFactoryManagers = new DeviceConnFactoryManager[4];
    private boolean isOpenPort = false;

    public enum CONN_METHOD {
        BLUETOOTH, USB
    }

    public static DeviceConnFactoryManager[] getDeviceConnFactoryManagers() {
        return deviceConnFactoryManagers;
    }

    private DeviceConnFactoryManager(Build build) {
        this.connMethod = build.connMethod;
        this.macAddress = build.macAddress;
        this.mUsbDevice = build.usbDevice;
        this.mContext = build.context;
        this.id = build.id;
        deviceConnFactoryManagers[id] = this;
    }

    public void openPort() {
        isOpenPort = false;
        try {
            if (connMethod == CONN_METHOD.USB) {
                mPort = new UsbPort(mContext, mUsbDevice);
                isOpenPort = mPort.openPort();
            } else if (connMethod == CONN_METHOD.BLUETOOTH) {
                mPort = new BluetoothPort(macAddress);
                isOpenPort = mPort.openPort();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closePort(int id) {
        if (this.mPort != null) {
            this.mPort.closePort();
            isOpenPort = false;
        }
    }

    public void sendDataImmediately(final Vector<Byte> data) {
        if (this.mPort == null) {
            return;
        }
        try {
            this.mPort.writeDataImmediately(data, 0, data.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final class Build {
        private String macAddress;
        private UsbDevice usbDevice;
        private CONN_METHOD connMethod;
        private Context context;
        private int id;

        public Build setMacAddress(String macAddress) {
            this.macAddress = macAddress;
            return this;
        }

        public Build setUsbDevice(UsbDevice usbDevice) {
            this.usbDevice = usbDevice;
            return this;
        }

        public Build setConnMethod(CONN_METHOD connMethod) {
            this.connMethod = connMethod;
            return this;
        }

        public Build setContext(Context context) {
            this.context = context;
            return this;
        }

        public Build setId(int id) {
            this.id = id;
            return this;
        }

        public DeviceConnFactoryManager build() {
            return new DeviceConnFactoryManager(this);
        }
    }
}
