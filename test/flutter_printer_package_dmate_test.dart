import 'package:flutter/services';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_printer_package_dmate/flutter_printer_package_dmate.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter_printer_package_dmate');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (MethodCall methodCall) async {
      if (methodCall.method == 'getUSBDevices') {
        return ['USB Printer 1'];
      }
      if (methodCall.method == 'getBluetoothDevices') {
        return [
          {'name': 'BT Printer 1', 'address': '00:11:22:33:44:55'}
        ];
      }
      if (methodCall.method == 'printReceipt') {
        return true;
      }
      return null;
    });
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
  });

  test('getUSBDevices', () async {
    expect(await FlutterPrinterPackageDmate.getUSBDevices(), ['USB Printer 1']);
  });

  test('getBluetoothDevices', () async {
    final bt = await FlutterPrinterPackageDmate.getBluetoothDevices();
    expect(bt.length, 1);
    expect(bt[0]['name'], 'BT Printer 1');
  });

  test('printReceipt', () async {
    expect(await FlutterPrinterPackageDmate.printReceipt(
      deviceModel: 'BPOS',
      connectionType: 'USB',
      printerData: {},
    ), true);
  });
}
