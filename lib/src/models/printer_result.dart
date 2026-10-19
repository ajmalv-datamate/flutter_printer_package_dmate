/// Structured result returned from printer operations.
class PrinterResult {
  final bool isSuccess;
  final String message;
  final String? errorCode;
  final dynamic rawDetails;

  const PrinterResult({
    required this.isSuccess,
    required this.message,
    this.errorCode,
    this.rawDetails,
  });

  factory PrinterResult.success([String message = 'Print job completed successfully']) {
    return PrinterResult(isSuccess: true, message: message);
  }

  factory PrinterResult.failure(String message, {String? errorCode, dynamic details}) {
    return PrinterResult(
      isSuccess: false,
      message: message,
      errorCode: errorCode,
      rawDetails: details,
    );
  }

  @override
  String toString() =>
      'PrinterResult(success: $isSuccess, message: "$message", code: $errorCode)';
}
