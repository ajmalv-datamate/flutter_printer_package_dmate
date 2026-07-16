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
All required SDK binaries (`.jar` and `.aar` files) are packaged locally inside the plugin's android/libs folder. There are no absolute sibling dependencies, making it plug-and-play when imported into any project or compiled on different systems.

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
Use the `printReceipt` function, passing the target model, connection type, path (e.g. bluetooth MAC address), and receipt data.

You can print in **two different modes**:
- **Structured Template Mode**: Pass key-value pairs (e.g., `"clientname"`, `"cartItems"`) and the package will layout the receipt automatically.
- **Raw Text Mode (Recommended for Existing App Migrations)**: Pass `"rawText"` containing your pre-formatted receipt string. The package will bypass template generation and print your monospaced text exactly as is. On bitmap-based printers (PineLab, Sunmi, Epson, TVS), the package will auto-render the raw text into a high-quality monospaced bitmap before printing.

```dart
import 'package:flutter_printer_package_dmate/flutter_printer_package_dmate.dart';

void startPrintingJob() async {
  // Option A: Raw Text Mode (Prints pre-formatted strings directly)
  Map<String, dynamic> rawReceiptData = {
    "rawText": """
    ================================
             CORNER CAFE
             Ground Floor
    ================================
    Bill No: Bill-9988
    Date:    08/07/2026 11:45 AM
    --------------------------------
    Cafe Latte         2x     200.00
    Butter Croissant   1x      80.00
    --------------------------------
    Total Payable:            280.00
    ================================
    """
  };

  // Option B: Structured Template Mode (Auto-formatted by plugin layout engine)
  Map<String, dynamic> structuredReceiptData = {
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

  // Trigger the print job (using Raw Text Mode as an example)
  bool isSuccess = await FlutterPrinterPackageDmate.printReceipt(
    deviceModel: "BPOS",       // Choose from: TVS, BPOS, POSIQ, IMIN, PINE_LAB, TSUNMI, EPSON
    connectionType: "USB",     // "USB" or "BLUETOOTH"
    connectionPath: "",        // Optional MAC address or USB device target
    printerData: rawReceiptData,
    options: {
      "paperWidth": 576,       // 576 pixels (for 80mm paper) or 384 pixels (for 58mm paper)
      "pineLabAppId": "your_plutus_app_id_here" // Optional. If left blank, uses coffeeshop defaults.
    }
  );

  if (isSuccess) {
    print("Receipt printed successfully!");
  } else {
    print("Print operation failed.");
  }
}
```

---

## 3. PineLab App ID & Security Registration

PineLab terminal services strictly validate the identity of the calling application. For prints to go through, the calling application's **Package Name** and its **Keystore Signature SHA-256 fingerprint** must be registered on the PineLab Developer Console.

### Coffeeshop Default Fallbacks
If no `"pineLabAppId"` is provided in the `options` parameter, the plugin automatically detects your app's package name and falls back to these default coffeeshop keys:
- **Package Name**: `com.hotsoft.coffeeshop` (Dev) -> **App ID**: `386e4aa24d324023b77efe76f607f0bb`
- **Package Name**: `com.hotsoft.coffeeshopprod` (Prod) -> **App ID**: `ca64099cf0a74e68af6aa4c9159283e5`

### Using in Other Applications
To use this package in other projects with different package names:
1. Register your new package name (e.g. `com.example.myapp`) with PineLab.
2. Obtain your custom **Application ID** from the PineLab portal.
3. Pass your custom ID dynamically in the print call:
   ```dart
   options: {
     "pineLabAppId": "your-new-custom-id-from-pinelab"
   }
   ```

---

## Supported Models Parameter Reference

| `deviceModel` | Description | Connection Types Supported | Layout Target |
|---|---|---|---|
| **`TVS`** | TVS USB printer (ESC/POS Class 7) | `USB` | Raster Bitmap |
| **`BPOS`** / **`BPOS_POS`** | BPOS mobile thermal printers (POSConnect/POSPrinter SDK) | `USB`, `BLUETOOTH` | Formatted Text / Bitmap |
| **`BPOS_NYX`** | BPOS mobile devices using internal Nyx Printer Service | Internal Bind (AIDL) | Formatted Text |
| **`POSIQ`** | PosIQ desktop thermal printers | `USB` | Formatted Text |
| **`IMIN`** / **`IMIN_POS`** | IMin integrated POS hardware | `USB` (internal binding) | Formatted Text |
| **`PINE_LAB`** | PineLab Smart Payment terminal | `USB` (service-bound) | Raster Bitmap |
| **`TSUNMI`** / **`TSUNMI_EXTERNAL`** | Sunmi integrated POS hardware | `USB` (service-bound) | Raster Bitmap |
| **`EPSON`** | Epson ePOS-Print desktop printers | `USB`, `BLUETOOTH` | Raster Bitmap |

---

## Native SDK Integration Guide in Flutter

This package bridges native manufacturer printer SDKs to Dart using Flutter **Method Channels**.

### How SDK Integration Works:

1. **AAR/JAR Libraries (`android/libs`)**:
   Add manufacturer library files (`.aar`, `.jar`) to the `android/libs` directory of the Flutter plugin/project.
   Ensure they are imported in `android/build.gradle`:
   ```groovy
   dependencies {
       implementation fileTree(dir: 'libs', include: ['*.jar', '*.aar'])
   }
   ```

2. **AIDL Interfaces (`android/src/main/aidl`)**:
   For devices using system service binders (like Nyx printers), copy the `.aidl` files defining the service contract into the correct package hierarchy inside `src/main/aidl/`.
   Enable AIDL compilation in `android/build.gradle`:
   ```groovy
   android {
       buildFeatures {
           aidl true
       }
   }
   ```

3. **Method Channel Bridge (`MethodChannel`)**:
   - In Dart, invoke print requests with arguments:
     ```dart
     final bool? success = await _channel.invokeMethod('printReceipt', arguments);
     ```
   - In Java/Kotlin (e.g. `FlutterPrinterPackageDmatePlugin.java`), override `onMethodCall` to catch the method name, extract arguments, and route to the corresponding native device integration class:
     ```java
     @Override
     public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
         if (call.method.equals("printReceipt")) {
             // Extract arguments and call native SDK print methods
         }
     }
     ```



void triggerTestPrint() async {
// 1. Prepare minimal test receipt data
Map<String, dynamic> testData = {
"clientname": "Test Cafe Hub",
"locationName": "Billing Desk 1",
"billNo": "Bill-0001",
"date": "08/07/2026 12:00 PM",
"total": "100.00",
"sgst": "5.00",
"cgst": "5.00",
"totalPayable": "110.00",
"amountInWords": "Rupees One Hundred and Ten Only",
"paymentMode": "CASH",
"cartItems": [
{
"itemName": "Standard Espresso",
"quantity": "1",
"price": "100.00",
"total": "100.00"
}
]
};
// 2. Call print API
bool success = await FlutterPrinterPackageDmate.printReceipt(
deviceModel: "PINE_LAB",       // Choose printer model: TVS, BPOS, POSIQ, IMIN, PINE_LAB, TSUNMI, EPSON
connectionType: "USB",     // Connection type: USB or BLUETOOTH
printerData: testData,
options: {
"paperWidth": 576,
// "pineLabAppId": "com.hotsoft.coffeeshop"
// Width in pixels: 576 (80mm) or 384 (58mm)
}
// com.hotsoft.coffeeshopprod
);
print("Print operation finished with status: $success");
}

ElevatedButton(onPressed: (){triggerTestPrint();}, child: Text("Test Print")),