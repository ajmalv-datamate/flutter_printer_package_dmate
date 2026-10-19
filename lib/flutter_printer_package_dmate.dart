import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

export 'src/models/printer_model_info.dart';
export 'src/models/printer_result.dart';
export 'src/widgets/printer_model_dropdown.dart';

import 'src/models/printer_model_info.dart';
import 'src/models/printer_result.dart';

/// Unified Flutter Printer Package for thermal receipt and Android POS printing.
/// 
/// Supports multiple hardware drivers out of the box:
/// - Landi Android POS (via OmniDriver SDK)
/// - BPOS / Standard POS-80 ESC/POS
/// - BPOS Nyx Handheld POS (58mm)
/// - TVS Thermal Printers (USB)
/// - POS-IQ Android POS
/// - iMin Android POS
/// - Pine Labs Plutus Smart POS
/// - Sunmi Built-in Thermal POS
/// - Epson ESC/POS (Network/USB)
class FlutterPrinterPackageDmate {
  static const MethodChannel _channel = MethodChannel('flutter_printer_package_dmate');

  /// Complete catalog of all supported printer hardware drivers and their specifications.
  static const List<PrinterModelInfo> supportedModels = [
    PrinterModelInfo(
      id: 'LANDI',
      displayName: 'Landi POS (OmniDriver)',
      manufacturer: 'Landi',
      description: 'Landi Android POS hardware via OmniDriver service',
      defaultPaperWidth: 384,
      paperSizeLabel: '58mm',
      isHandheld: true,
      icon: Icons.point_of_sale_rounded,
      accentColor: Color(0xFF0EA5E9),
    ),
    PrinterModelInfo(
      id: 'BPOS',
      displayName: 'BPOS / POS-80',
      manufacturer: 'BPOS',
      description: 'Standard USB / Bluetooth 80mm thermal receipt printer',
      defaultPaperWidth: 576,
      paperSizeLabel: '80mm',
      isHandheld: false,
      icon: Icons.print_rounded,
      accentColor: Color(0xFF7C3AED),
    ),
    PrinterModelInfo(
      id: 'BPOS_NYX',
      displayName: 'BPOS NYX (Handheld)',
      manufacturer: 'Nyx',
      description: 'BPOS Nyx 58mm portable handheld Android POS terminal',
      defaultPaperWidth: 384,
      paperSizeLabel: '58mm',
      isHandheld: true,
      icon: Icons.phone_android_rounded,
      accentColor: Color(0xFF6366F1),
    ),
    PrinterModelInfo(
      id: 'TVS',
      displayName: 'TVS Thermal Printer',
      manufacturer: 'TVS',
      description: 'TVS direct USB thermal receipt printer',
      defaultPaperWidth: 576,
      paperSizeLabel: '80mm',
      isHandheld: false,
      icon: Icons.usb_rounded,
      accentColor: Color(0xFFF59E0B),
    ),
    PrinterModelInfo(
      id: 'POSIQ',
      displayName: 'POS-IQ Terminal',
      manufacturer: 'POS-IQ',
      description: 'POS-IQ Android integrated thermal billing hardware',
      defaultPaperWidth: 576,
      paperSizeLabel: '80mm',
      isHandheld: false,
      icon: Icons.devices_other_rounded,
      accentColor: Color(0xFF10B981),
    ),
    PrinterModelInfo(
      id: 'IMIN',
      displayName: 'iMin Android POS',
      manufacturer: 'iMin',
      description: 'iMin Android desktop & handheld POS thermal printers',
      defaultPaperWidth: 576,
      paperSizeLabel: '80mm',
      isHandheld: false,
      icon: Icons.tablet_android_rounded,
      accentColor: Color(0xFFEC4899),
    ),
    PrinterModelInfo(
      id: 'PINE_LAB',
      displayName: 'Pine Labs (Plutus)',
      manufacturer: 'Pine Labs',
      description: 'Pine Labs Plutus smart payment POS terminal',
      defaultPaperWidth: 384,
      paperSizeLabel: '58mm',
      isHandheld: true,
      icon: Icons.credit_card_rounded,
      accentColor: Color(0xFF14B8A6),
    ),
    PrinterModelInfo(
      id: 'SUNMI',
      displayName: 'Sunmi POS Terminal',
      manufacturer: 'Sunmi',
      description: 'Sunmi integrated thermal printer service',
      defaultPaperWidth: 576,
      paperSizeLabel: '80mm',
      isHandheld: false,
      icon: Icons.store_mall_directory_rounded,
      accentColor: Color(0xFFF97316),
    ),
    PrinterModelInfo(
      id: 'EPSON',
      displayName: 'Epson ESC/POS',
      manufacturer: 'Epson',
      description: 'Epson TM series network TCP / USB thermal receipt printer',
      defaultPaperWidth: 576,
      paperSizeLabel: '80mm',
      isHandheld: false,
      icon: Icons.lan_rounded,
      accentColor: Color(0xFF2563EB),
    ),
  ];

  /// List of raw model ID strings (e.g. ['LANDI', 'BPOS', 'BPOS_NYX', ...])
  static List<String> get modelIds => supportedModels.map((m) => m.id).toList();

  /// Checks whether a given model string is known and supported.
  static bool isSupported(String? modelId) {
    if (modelId == null || modelId.trim().isEmpty) return false;
    final normalized = normalizeModel(modelId);
    return supportedModels.any((m) => m.id == normalized);
  }

  /// Normalizes aliases and typos to canonical model IDs.
  static String normalizeModel(String? modelId) {
    if (modelId == null || modelId.trim().isEmpty) return 'BPOS';
    final cleaned = modelId.trim().toUpperCase();

    if (cleaned == 'LANDI' || cleaned == 'LANDI_POS' || cleaned == 'LANDIPOS') {
      return 'LANDI';
    }
    if (cleaned == 'BPOS' || cleaned == 'BPOS_POS' || cleaned == 'POS80' || cleaned == 'POS-80') {
      return 'BPOS';
    }
    if (cleaned == 'BPOS_NYX' || cleaned == 'BPOSNYX' || cleaned == 'NYX') {
      return 'BPOS_NYX';
    }
    if (cleaned == 'TVS') {
      return 'TVS';
    }
    if (cleaned == 'POSIQ' || cleaned == 'POS_IQ') {
      return 'POSIQ';
    }
    if (cleaned == 'IMIN' || cleaned == 'IMIN_POS') {
      return 'IMIN';
    }
    if (cleaned == 'PINE_LAB' || cleaned == 'PINELAB' || cleaned == 'PLUTUS') {
      return 'PINE_LAB';
    }
    if (cleaned == 'SUNMI' || cleaned == 'TSUNMI' || cleaned == 'TSUNMI_EXTERNAL' || cleaned == 'TSUNMI_TAB') {
      return 'SUNMI';
    }
    if (cleaned == 'EPSON') {
      return 'EPSON';
    }

    return cleaned;
  }

  /// Retrieves metadata specifications for a given model ID.
  static PrinterModelInfo getModelInfo(String? modelId) {
    final normalized = normalizeModel(modelId);
    return supportedModels.firstWhere(
      (m) => m.id == normalized,
      orElse: () => supportedModels.firstWhere((m) => m.id == 'BPOS'),
    );
  }

  /// Fetches a list of connected USB device names.
  static Future<List<String>> getUSBDevices() async {
    try {
      final List<dynamic>? devices = await _channel.invokeMethod('getUSBDevices');
      return devices?.cast<String>() ?? [];
    } catch (_) {
      return [];
    }
  }

  /// Fetches a list of paired Bluetooth devices containing 'name' and 'address'.
  static Future<List<Map<String, String>>> getBluetoothDevices() async {
    try {
      final List<dynamic>? devices = await _channel.invokeMethod('getBluetoothDevices');
      if (devices == null) return [];
      return devices.map((d) => Map<String, String>.from(d as Map)).toList();
    } catch (_) {
      return [];
    }
  }

  /// Prints a receipt using the specified printer model, connection details, and data.
  /// 
  /// [deviceModel] should be one of [supportedModels] (e.g. 'LANDI', 'BPOS', 'BPOS_NYX', 'TVS', 'POSIQ', 'IMIN', 'PINE_LAB', 'SUNMI', 'EPSON').
  /// [connectionType] should be: 'USB', 'BLUETOOTH', 'NETWORK' (or 'TCP').
  /// [connectionPath] MAC Address for Bluetooth, IP address for Network/TCP (e.g. "192.168.1.100"), target path or "USB:" for USB.
  /// [printerData] map matching the java class PrinterData.
  /// [options] optional configuration map like:
  ///   `paperWidth` (int): 576 (for 80mm) or 384 (for 58mm)
  ///   `pineLabAppId` (String): Plutus smart App ID for PineLab.
  ///   `printerSeries` (String): Epson printer series (e.g. "TM_T88", "TM_T82", "TM_T20", "TM_M30"). Default is "TM_T88".
  static Future<bool> printReceipt({
    required String deviceModel,
    required String connectionType,
    String? connectionPath,
    required Map<String, dynamic> printerData,
    Map<String, dynamic>? options,
  }) async {
    final result = await printReceiptDetailed(
      deviceModel: deviceModel,
      connectionType: connectionType,
      connectionPath: connectionPath,
      printerData: printerData,
      options: options,
    );
    if (!result.isSuccess) {
      throw Exception(result.message);
    }
    return true;
  }

  /// Detailed print execution returning a structured [PrinterResult] containing
  /// success status, error code, and descriptive message.
  static Future<PrinterResult> printReceiptDetailed({
    required String deviceModel,
    required String connectionType,
    String? connectionPath,
    required Map<String, dynamic> printerData,
    Map<String, dynamic>? options,
  }) async {
    final normalizedModel = normalizeModel(deviceModel);

    // Automatically assign default paper width if not provided in options
    final resolvedOptions = Map<String, dynamic>.from(options ?? {});
    if (!resolvedOptions.containsKey('paperWidth')) {
      final info = getModelInfo(normalizedModel);
      resolvedOptions['paperWidth'] = info.defaultPaperWidth;
    }

    try {
      final bool? result = await _channel.invokeMethod('printReceipt', {
        'deviceModel': normalizedModel,
        'connectionType': connectionType.toUpperCase(),
        'connectionPath': connectionPath,
        'printerData': printerData,
        'options': resolvedOptions,
      });

      if (result == true) {
        return PrinterResult.success('Receipt printed successfully on $normalizedModel');
      } else {
        return PrinterResult.failure('Print job completed with false result on $normalizedModel');
      }
    } on PlatformException catch (e) {
      return PrinterResult.failure(
        _formatPlatformError(normalizedModel, e),
        errorCode: e.code,
        details: e.details,
      );
    } catch (e) {
      return PrinterResult.failure(
        'Unexpected error printing to $normalizedModel: $e',
      );
    }
  }

  static String _formatPlatformError(String model, PlatformException e) {
    final code = e.code.toUpperCase();
    final message = e.message ?? 'Unknown error';

    if (code == 'LANDI_PRINT_ERROR' || code == 'LANDI_STATUS_ERROR') {
      return 'Landi Printer Error: $message';
    }
    if (code.contains('BIND') || code.contains('DISCONNECTED')) {
      return 'Printer hardware service disconnected or not installed for $model ($message)';
    }
    if (code == 'PERMISSION_DENIED') {
      return 'USB or Bluetooth permission was denied for the printer.';
    }
    if (code == 'NO_PRINTER' || code == 'CONNECT_ERROR') {
      return 'Printer not connected or device not found. Please check cable or power.';
    }
    return '$message (Code: ${e.code})';
  }
}
