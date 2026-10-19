import 'package:flutter/material.dart';
import '../../flutter_printer_package_dmate.dart';

/// An auto-defined dropdown component providing clean, standardized selection of
/// all thermal printer hardware driver models supported by flutter_printer_package_dmate.
///
/// Can be dropped into any Flutter app without having to re-declare printer lists,
/// paper sizes, or dropdown styling.
class PrinterModelDropdown extends StatelessWidget {
  /// The currently selected model identifier (e.g. 'LANDI', 'BPOS', 'IMIN', 'AUTO').
  final String? value;

  /// Callback when a printer model is selected.
  final ValueChanged<String?> onChanged;

  /// Whether to include an automatic "AUTO" (ERP Location Default) option at the top.
  final bool includeAutoOption;

  /// The ERP backend printer model name to display in the AUTO option (e.g. "BPOS").
  final String? autoModelName;

  /// Label text for the input decoration. Defaults to "Select Hardware Driver Model".
  final String? labelText;

  /// Optional custom hint text.
  final String? hintText;

  /// Custom prefix icon. Defaults to Icons.point_of_sale_rounded.
  final Widget? prefixIcon;

  /// Whether the dropdown should be visually dense. Defaults to true.
  final bool isDense;

  /// Background color of the dropdown popup menu.
  final Color? dropdownColor;

  /// Primary theme accent color for badges and highlights.
  final Color? accentColor;

  /// Custom input decoration override.
  final InputDecoration? decoration;

  /// Whether the dropdown is enabled.
  final bool enabled;

  /// Optional form validation callback.
  final FormFieldValidator<String>? validator;

  /// Whether to show paper width badges (e.g. "58mm", "80mm") next to each model.
  final bool showPaperSizeBadges;

  const PrinterModelDropdown({
    super.key,
    required this.value,
    required this.onChanged,
    this.includeAutoOption = false,
    this.autoModelName,
    this.labelText = "Select Hardware Driver Model",
    this.hintText,
    this.prefixIcon,
    this.isDense = true,
    this.dropdownColor,
    this.accentColor,
    this.decoration,
    this.enabled = true,
    this.validator,
    this.showPaperSizeBadges = true,
  });

  Widget _buildAutoItem(Color effectiveAccent) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(Icons.auto_mode_rounded, size: 16, color: effectiveAccent),
        const SizedBox(width: 8),
        Text(
          autoModelName != null && autoModelName!.isNotEmpty
              ? "ERP Location Default ($autoModelName)"
              : "ERP Location Default (Auto)",
          style: TextStyle(
            fontWeight: FontWeight.w700,
            color: effectiveAccent,
            fontSize: 13.5,
          ),
        ),
      ],
    );
  }

  Widget _buildModelItem(PrinterModelInfo model, bool isDark) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(model.icon, size: 16, color: model.accentColor),
        const SizedBox(width: 8),
        Text(
          model.displayName,
          style: TextStyle(
            fontWeight: FontWeight.w600,
            color: isDark ? Colors.white : const Color(0xFF0F172A),
            fontSize: 13.5,
          ),
        ),
        if (showPaperSizeBadges) ...[
          const SizedBox(width: 8),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 5, vertical: 1.5),
            decoration: BoxDecoration(
              color: model.isHandheld
                  ? const Color(0xFF3B82F6).withValues(alpha: 0.12)
                  : const Color(0xFF10B981).withValues(alpha: 0.12),
              borderRadius: BorderRadius.circular(4),
            ),
            child: Text(
              model.paperSizeLabel,
              style: TextStyle(
                fontSize: 9.5,
                fontWeight: FontWeight.w700,
                color: model.isHandheld
                    ? const Color(0xFF2563EB)
                    : const Color(0xFF059669),
              ),
            ),
          ),
        ],
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    final effectiveAccent = accentColor ?? const Color(0xFF7C3AED);
    final isDark = Theme.of(context).brightness == Brightness.dark;

    // Resolve effective selected value safely against supported models
    final String? effectiveValue;
    if (value != null && value!.isNotEmpty) {
      if (includeAutoOption && value == 'AUTO') {
        effectiveValue = 'AUTO';
      } else if (FlutterPrinterPackageDmate.isSupported(value)) {
        effectiveValue = FlutterPrinterPackageDmate.normalizeModel(value);
      } else if (includeAutoOption) {
        effectiveValue = 'AUTO';
      } else {
        effectiveValue = 'BPOS';
      }
    } else {
      effectiveValue = includeAutoOption ? 'AUTO' : 'BPOS';
    }

    final defaultDecoration = InputDecoration(
      isDense: isDense,
      contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
      labelText: labelText,
      labelStyle: TextStyle(
        fontSize: 12.5,
        color: isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B),
      ),
      hintText: hintText,
      prefixIcon: prefixIcon ??
          Icon(Icons.point_of_sale_rounded, size: 19, color: effectiveAccent),
      filled: true,
      fillColor: isDark ? const Color(0xFF141620) : Colors.white,
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(10),
        borderSide: BorderSide(
          color: isDark ? const Color(0xFF262A38) : const Color(0xFFE2E8F0),
        ),
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(10),
        borderSide: BorderSide(
          color: isDark ? const Color(0xFF262A38) : const Color(0xFFE2E8F0),
        ),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(10),
        borderSide: BorderSide(color: effectiveAccent, width: 1.5),
      ),
    );

    return DropdownButtonFormField<String>(
      value: effectiveValue,
      isExpanded: true,
      dropdownColor: dropdownColor ?? (isDark ? const Color(0xFF141620) : Colors.white),
      style: TextStyle(
        color: isDark ? Colors.white : const Color(0xFF0F172A),
        fontSize: 13.5,
        fontWeight: FontWeight.w600,
      ),
      decoration: decoration ?? defaultDecoration,
      validator: validator,
      items: [
        if (includeAutoOption)
          DropdownMenuItem<String>(
            value: 'AUTO',
            child: _buildAutoItem(effectiveAccent),
          ),
        ...FlutterPrinterPackageDmate.supportedModels.map((PrinterModelInfo model) {
          return DropdownMenuItem<String>(
            value: model.id,
            child: _buildModelItem(model, isDark),
          );
        }),
      ],
      onChanged: enabled ? onChanged : null,
    );
  }
}
