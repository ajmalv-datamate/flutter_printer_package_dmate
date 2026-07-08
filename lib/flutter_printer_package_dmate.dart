import 'dart:async';
import 'package:flutter/services';

class FlutterPrinterPackageDmate {
  static const MethodChannel _channel = MethodChannel('flutter_printer_package_dmate');

  /// Fetches a list of connected USB device names.
  static Future<List<String>> getUSBDevices() async {
    final List<dynamic>? devices = await _channel.invokeMethod('getUSBDevices');
    return devices?.cast<String>() ?? [];
  }

  /// Fetches a list of paired Bluetooth devices containing 'name' and 'address'.
  static Future<List<Map<String, String>>> getBluetoothDevices() async {
    final List<dynamic>? devices = await _channel.invokeMethod('getBluetoothDevices');
    if (devices == null) return [];
    return devices.map((d) => Map<String, String>.from(d as Map)).toList();
  }

  /// Prints a receipt using the specified printer model, connection details, and data.
  /// 
  /// [deviceModel] should be one of: TVS, BPOS, POSIQ, IMIN, IMIN_POS, PINE_LAB, TSUNMI, EPSON.
  /// [connectionType] should be: USB, BLUETOOTH.
  /// [connectionPath] MAC Address for Bluetooth, target path or "USB:" for USB.
  /// [printerData] map matching the java class PrinterData.
  /// [options] optional configuration map like:
  ///   `paperWidth` (int): 576 (for 80mm) or 384 (for 58mm)
  ///   `pineLabAppId` (String): Plutus smart App ID for PineLab.
  static Future<bool> printReceipt({
    required String deviceModel,
    required String connectionType,
    String? connectionPath,
    required Map<String, dynamic> printerData,
    Map<String, dynamic>? options,
  }) async {
    final bool? result = await _channel.invokeMethod('printReceipt', {
      'deviceModel': deviceModel,
      'connectionType': connectionType,
      'connectionPath': connectionPath,
      'printerData': printerData,
      'options': options,
    });
    return result ?? false;
  }
}
