## 0.2.0

* **LANDI POS SDK Integration**:
  - Integrated official Landi OmniDriver SDK (`xsuite-omnidriver-api-0.11.3.aar`).
  - Native service binding via `LandiPrinterIntegration.java` with automatic status detection, image raster printing, paper feed, and safe cutter handling.
* **Auto-Defined UI Component**:
  - Added `PrinterModelDropdown` widget for plug-and-play printer hardware selection in any Flutter project.
  - Added visual squircle icons, paper width badges (58mm/80mm), and automatic dark mode support.
  - Added optional ERP Location Default (`AUTO`) option support.
* **Architecture & Error Handling**:
  - Added `PrinterModelInfo` model catalog.
  - Added `PrinterResult` structured return type with standardized error codes (`DRIVER_NOT_FOUND`, `SERVICE_DISCONNECTED`, `DEVICE_ERROR`, `PRINT_FAILED`).
  - Added `printReceiptDetailed` API with human-friendly error messages.
  - Added helper methods: `supportedModels`, `modelIds`, `normalizeModel`, `isSupported`, `getModelInfo`.

## 0.1.0

* Initial multi-model printer support for TVS, BPOS, POSIQ, IMIN, PINE_LAB, SUNMI, and EPSON.
