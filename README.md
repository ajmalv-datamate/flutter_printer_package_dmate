# flutter_printer_package_dmate

A unified Flutter thermal printer plugin for direct POS receipt printing across all major manufacturer SDKs (`LANDI`, `BPOS`, `BPOS_NYX`, `TVS`, `POSIQ`, `IMIN`, `PINE_LAB`, `SUNMI`, `EPSON`) with a single method call.

Includes an **auto-defined, reusable dropdown UI widget** (`PrinterModelDropdown`) so any client application can drop in hardware selection without writing duplicate UI code.

---

## 🌟 What's New in this Update

- **🚀 LANDI Printer SDK Integration**:
  - Full native support for Landi Android POS terminals using the official **Landi OmniDriver SDK** (`xsuite-omnidriver-api-0.11.3.aar`).
  - Automatic printer service binding, hardware status checks, high-resolution bitmap raster printing (384px / 58mm), and safe cutter fallbacks.
- **🎨 Auto-Defined Reusable Dropdown (`PrinterModelDropdown`)**:
  - Plug-and-play UI dropdown widget that lists all supported thermal printers with manufacturer icons, paper size badges (`58mm` / `80mm`), and dark mode styling.
  - Optional support for ERP location default selection (`includeAutoOption: true`).
- **🛡️ Enhanced Error Handling (`PrinterResult`)**:
  - New `printReceiptDetailed(...)` method returning human-friendly messages, error codes (`DRIVER_NOT_FOUND`, `SERVICE_DISCONNECTED`, `DEVICE_ERROR`, `PRINT_FAILED`), and raw error details.
- **📚 Centralized Model Catalog (`FlutterPrinterPackageDmate.supportedModels`)**:
  - Standardized `PrinterModelInfo` model describing device type, default paper width, paper size labels, and brand accent colors.
  - Helper functions: `normalizeModel()`, `isSupported()`, and `getModelInfo()`.

---

## 🖨️ Supported Printer Hardware

| Model ID | Hardware / Manufacturer | Form Factor | Paper Width | Native Integration |
|---|---|---|---|---|
| **`LANDI`** | Landi POS Terminal | Handheld | **58mm** (384px) | Landi OmniDriver SDK (`.aar`) |
| **`BPOS`** | BPOS Thermal Printer | Desktop | **80mm** (576px) | POSPrinter SDK (`.jar`) / USB / Bluetooth |
| **`BPOS_NYX`** | BPOS NYX POS Terminal | Handheld | **58mm** (384px) | Nyx Printer Service (AIDL) |
| **`TVS`** | TVS Electronics Thermal Printer | Desktop | **80mm** (576px) | Sewoo POS SDK (`.aar`) |
| **`POSIQ`** | PosIQ Thermal Printer | Desktop | **80mm** (576px) | PosIQ USB/Serial SDK |
| **`IMIN`** | iMin POS Hardware | Desktop/Handheld | **80mm** (576px) | Imin Printer Service |
| **`PINE_LAB`** | Pine Labs POS Terminal | Handheld | **58mm** (384px) | Pine Labs MasterApp Service |
| **`SUNMI`** | Sunmi POS Terminal | Handheld | **58mm** (384px) | Sunmi Aidl Service |
| **`EPSON`** | Epson ESC/POS Printer | Desktop | **80mm** (576px) | Epson ePOS-Print SDK |

---

## 📦 Installation

Add `flutter_printer_package_dmate` to your `pubspec.yaml`:

```yaml
dependencies:
  flutter_printer_package_dmate:
    path: ../flutter_printer_package_dmate # or git url
```

---

## ⚙️ Android Configuration & Permissions

Ensure your app's `android/app/src/main/AndroidManifest.xml` includes the necessary permissions and `<queries>` tags for Android 11+ (API 30+):

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- General Network and Printing Permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="com.pax.permission.PRINTER" />

    <!-- USB Host & Permissions -->
    <uses-feature android:name="android.hardware.usb.host" android:required="false" />
    <uses-permission android:name="android.permission.USB_PERMISSION" />

    <!-- Bluetooth Permissions -->
    <uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:maxSdkVersion="30" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

    <!-- Service Visibility Queries (Required for Android 11+) -->
    <queries>
        <!-- BPOS Nyx Printer Service -->
        <package android:name="net.nyx.printerservice" />
        <!-- PineLab MasterApp Service -->
        <package android:name="com.pinelabs.masterapp" />
        <!-- Neo / Incar / Landi Driver Services -->
        <package android:name="com.incar.printerservice" />
        <package android:name="com.neo.printer.sdk" />
        <package android:name="com.landicorp.android.eptapi" />
        <intent>
            <action android:name="com.neo.printer.sdk.core.ApiAdapterManager.NeoPrinterService" />
        </intent>
    </queries>
</manifest>
```

---

## 🚀 How to Use in Any Flutter Project

### 1. Reusable Printer Dropdown (`PrinterModelDropdown`)
Instead of hardcoding printer lists and custom UI in your settings page, use `PrinterModelDropdown`:

```dart
import 'package:flutter/material.dart';
import 'package:flutter_printer_package_dmate/flutter_printer_package_dmate.dart';

class MyPrinterSettings extends StatefulWidget {
  const MyPrinterSettings({super.key});

  @override
  State<MyPrinterSettings> createState() => _MyPrinterSettingsState();
}

class _MyPrinterSettingsState extends State<MyPrinterSettings> {
  String _selectedModel = 'BPOS';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Printer Selection')),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            // Dropdown automatically loads all supported hardware drivers
            PrinterModelDropdown(
              value: _selectedModel,
              labelText: "Select Hardware Driver",
              onChanged: (String? newModel) {
                if (newModel != null) {
                  setState(() => _selectedModel = newModel);
                }
              },
            ),
          ],
        ),
      ),
    );
  }
}
```

#### Including ERP Location Default (AUTO) Option:
```dart
PrinterModelDropdown(
  value: _selectedModel,
  includeAutoOption: true,
  autoModelName: "BPOS", // The backend ERP model name
  onChanged: (val) => setState(() => _selectedModel = val!),
)
```

---

### 2. Single-Call Printing with Detailed Error Handling

Use `printReceiptDetailed` to get clear, human-readable error messages and error codes:

```dart
import 'dart:typed_data';
import 'package:flutter_printer_package_dmate/flutter_printer_package_dmate.dart';

Future<void> printBill(Uint8List imageBytes, String chosenModel) async {
  final PrinterResult result = await FlutterPrinterPackageDmate.printReceiptDetailed(
    model: chosenModel,          // 'LANDI', 'BPOS', 'SUNMI', etc.
    bytes: imageBytes,           // Pre-rendered receipt image bytes (PNG)
  );

  if (result.isSuccess) {
    print("Receipt printed successfully!");
  } else {
    // Show friendly error message to the user:
    print("Error: ${result.message}");
    print("Error Code: ${result.errorCode}"); // e.g. 'DRIVER_NOT_FOUND', 'PRINT_FAILED'
  }
}
```

---

### 3. Quick Printing with Raw Text or Template Data

If you have structured JSON receipt data or raw monospaced text, use `printReceipt`:

```dart
import 'package:flutter_printer_package_dmate/flutter_printer_package_dmate.dart';

bool success = await FlutterPrinterPackageDmate.printReceipt(
  deviceModel: "LANDI", // or "BPOS", "SUNMI", "IMIN", etc.
  connectionType: "USB",
  printerData: {
    "rawText": """
    ================================
              CAFE DEMO
    ================================
    Item               Qty    Price
    Cappuccino          1     120.00
    Croissant           2     160.00
    --------------------------------
    Total:                    280.00
    ================================
    """
  },
  options: {
    "paperWidth": 384, // 384 for 58mm, 576 for 80mm
  },
);
```

---

### 4. Hardware Model Inspection & Helpers

```dart
// 1. Get the list of all supported model IDs:
List<String> models = FlutterPrinterPackageDmate.modelIds;
// ['LANDI', 'BPOS', 'BPOS_NYX', 'TVS', 'POSIQ', 'IMIN', 'PINE_LAB', 'SUNMI', 'EPSON']

// 2. Normalize user or server strings (handles aliases like "LANDI_POS", "TSUNMI"):
String normalized = FlutterPrinterPackageDmate.normalizeModel("LANDI_POS"); // Returns "LANDI"

// 3. Check if a model is supported:
bool supported = FlutterPrinterPackageDmate.isSupported("LANDI"); // true

// 4. Inspect model metadata:
PrinterModelInfo? info = FlutterPrinterPackageDmate.getModelInfo("LANDI");
print("Name: ${info?.displayName}");       // "Landi POS"
print("Paper: ${info?.paperSizeLabel}");    // "58mm"
print("Width: ${info?.defaultPaperWidth}"); // 384
```

---

## 🛠️ Error Codes Reference

When calling `printReceiptDetailed`, `result.errorCode` can be one of:

| Error Code | Meaning | Recommended Action |
|---|---|---|
| `EMPTY_IMAGE` | Receipt image bytes were empty or null. | Verify receipt rendering pipeline. |
| `UNSUPPORTED_MODEL` | Device model string is not recognized. | Use `FlutterPrinterPackageDmate.normalizeModel()`. |
| `DRIVER_NOT_FOUND` | Hardware driver service is missing or not installed on Android. | Install the manufacturer driver app/service. |
| `SERVICE_DISCONNECTED` | POS system service disconnected unexpectedly. | Restart POS terminal printer service. |
| `DEVICE_ERROR` | Hardware issue reported (e.g., out of paper, cover open, overheating). | Check thermal paper roll and close printer cover. |
| `PRINT_FAILED` | General print error from driver SDK. | Inspect `result.rawDetails` for native stack trace. |

---

## 📄 License
MIT License.
