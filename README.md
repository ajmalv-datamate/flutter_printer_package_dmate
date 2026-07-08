# flutter_printer_package_dmate

A unified Flutter printer package that enables direct printing to various POS and external thermal printers (`TVS`, `BPOS`, `POSIQ`, `IMIN`, `PINE_LAB`, `TSUNMI`, `EPSON`) with a single method call.

This plugin bridges the native Android SDKs for each manufacturer and offers formatting helper utilities (both text-based and graphical bitmap formats).

---

## Features

- **Unified Printing**: Print receipts across multiple different device models using a single Dart API call.
- **Dynamic Formatting**: Generates standard alignment layouts automatically.
- **Peripheral Discovery**: Helper calls to query active USB and paired Bluetooth printers.
- **Integrated SDK Support**: Works with standard manufacturer packages (Sewoo TVS, POSPrinter BPOS, PosIQ USB/Serial, Imin Service, Sunmi Aidl, Epson ePOS-Print, and PineLab MasterApp).

---

## Android Setup & Requirements

### Native Dependency Resolution
To avoid duplicating large SDK binaries (`.jar` and `.aar` files), the plugin compiles directly against the library files of the sibling project:
`D:/Ajmal/BMHBILLING/app/libs`

If your workspace path differs, you can update the repository flat directory inside `android/build.gradle`:
```groovy
repositories {
    flatDir {
        dirs 'libs', 'D:/Ajmal/BMHBILLING/app/libs'
    }
}
```

### Permissions
Ensure the following permissions are configured in your application's `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<queries>
    <package android:name="com.pinelabs.masterapp" />
</queries>
```

---

## API & How to Call

### 1. Discover Connected Printers
You can list active USB devices or paired Bluetooth devices:

```dart
import 'package:flutter_printer_package_dmate/flutter_printer_package_dmate.dart';

// List connected USB devices
List<String> usbPrinters = await FlutterPrinterPackageDmate.getUSBDevices();

// List paired Bluetooth devices
List<Map<String, String>> bluetoothPrinters = await FlutterPrinterPackageDmate.getBluetoothDevices();
for (var printer in bluetoothPrinters) {
  print("Name: ${printer['name']}, Address: ${printer['address']}");
}
```

### 2. Print Receipt
Use the `printReceipt` function, passing the target model, connection type, path (e.g. bluetooth MAC address), and receipt data:

```dart
import 'package:flutter_printer_package_dmate/flutter_printer_package_dmate.dart';

void startPrintingJob() async {
  // 1. Prepare receipt fields
  Map<String, dynamic> receiptData = {
    "clientname": "Corner Cafe",
    "locationName": "Ground Floor",
    "locationName1": "Sector-3, Bypass Road",
    "locationName2": "Kochi, Kerala",
    "billNo": "Bill-9988",
    "date": "08/07/2026 11:45 AM",
    "total": "280.00",
    "sgst": "14.00",
    "cgst": "14.00",
    "totalPayable": "308.00",
    "amountInWords": "Rupees Three Hundred and Eight Only",
    "paymentMode": "CASH",
    "outlet": "Kochi Bypass Hub",
    "gstIN": "32ABCDE1234F1Z0",
    "fssai": "12345678901234",
    "table": "T-12",
    "waiter": "Alex",
    "remarks": "Extra Hot",
    "kot": "KOT-342",
    "cartItems": [
      {
        "itemName": "Cafe Latte",
        "quantity": "2",
        "price": "100.00",
        "total": "200.00"
      },
      {
        "itemName": "Butter Croissant",
        "quantity": "1",
        "price": "80.00",
        "total": "80.00"
      }
    ]
  };

  // 2. Trigger the print job
  bool isSuccess = await FlutterPrinterPackageDmate.printReceipt(
    deviceModel: "BPOS",       // Choose from: TVS, BPOS, POSIQ, IMIN, PINE_LAB, TSUNMI, EPSON
    connectionType: "USB",     // "USB" or "BLUETOOTH"
    connectionPath: "",        // Optional MAC address or USB device target
    printerData: receiptData,
    options: {
      "paperWidth": 576,       // 576 pixels (for 80mm paper) or 384 pixels (for 58mm paper)
      "pineLabAppId": "your_plutus_app_id_here" // Mandatory for PINE_LAB prints
    }
  );

  if (isSuccess) {
    print("Receipt printed successfully!");
  } else {
    print("Print operation failed or cancelled.");
  }
}
```

---

## Supported Models Parameter Reference

| `deviceModel` | Description | Connection Types Supported | Layout Target |
|---|---|---|---|
| **`TVS`** | TVS USB printer (ESC/POS Class 7) | `USB` | Raster Bitmap |
| **`BPOS`** / **`BPOS_POS`** | BPOS mobile thermal printers | `USB`, `BLUETOOTH` | Formatted Text / Bitmap |
| **`POSIQ`** | PosIQ desktop thermal printers | `USB` | Formatted Text |
| **`IMIN`** / **`IMIN_POS`** | IMin integrated POS hardware | `USB` (internal binding) | Formatted Text |
| **`PINE_LAB`** | PineLab Smart Payment terminal | `USB` (service-bound) | Raster Bitmap |
| **`TSUNMI`** / **`TSUNMI_EXTERNAL`** | Sunmi integrated POS hardware | `USB` (service-bound) | Raster Bitmap |
| **`EPSON`** | Epson ePOS-Print desktop printers | `USB`, `BLUETOOTH` | Raster Bitmap |
