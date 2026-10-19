import 'package:flutter/material.dart';

/// Metadata describing a supported printer hardware driver model.
class PrinterModelInfo {
  /// Unique identifier (e.g. 'LANDI', 'BPOS', 'BPOS_NYX', 'TVS', 'POSIQ', 'IMIN', 'PINE_LAB', 'SUNMI', 'EPSON')
  final String id;

  /// User-friendly display title
  final String displayName;

  /// Hardware manufacturer / family
  final String manufacturer;

  /// Description of the printer connection and usage
  final String description;

  /// Default pixel print width (384 for 58mm, 576 for 80mm)
  final int defaultPaperWidth;

  /// Paper size label (e.g. '58mm', '80mm')
  final String paperSizeLabel;

  /// Whether this model represents a mobile/handheld POS terminal
  final bool isHandheld;

  /// Recommended icon representing this hardware
  final IconData icon;

  /// Brand accent color
  final Color accentColor;

  const PrinterModelInfo({
    required this.id,
    required this.displayName,
    required this.manufacturer,
    required this.description,
    required this.defaultPaperWidth,
    required this.paperSizeLabel,
    required this.isHandheld,
    required this.icon,
    this.accentColor = const Color(0xFF7C3AED),
  });

  @override
  String toString() => '$displayName ($id - $paperSizeLabel)';

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is PrinterModelInfo && runtimeType == other.runtimeType && id == other.id;

  @override
  int get hashCode => id.hashCode;
}
