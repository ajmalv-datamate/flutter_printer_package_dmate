package com.dmate.printer_package;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class PineLabDeviceIntegration {
    Context context;
    PineLabDeviceResponse pineLabDeviceResponse;
    Bitmap bitmap;
    String transactionType;
    String paymentAmount;
    String appId;

    public PineLabDeviceIntegration(Context context, String transactionType, String paymentAmount, String appId,
            Bitmap bitmap, PineLabDeviceResponse pineLabDeviceResponse) {
        this.context = context;
        this.pineLabDeviceResponse = pineLabDeviceResponse;
        this.transactionType = transactionType;
        this.paymentAmount = paymentAmount;
        this.appId = appId;
        this.bitmap = bitmap;
    }

    private static final String BILLING_REQUEST_TAG = "MASTERAPPREQUEST";
    private static final String BILLING_RESPONSE_TAG = "MASTERAPPRESPONSE";

    public void initialise(Context context) {
        String PLUTUS_SMART_ACTION = "com.pinelabs.masterapp.SERVER";
        String PLUTUS_SMART_PACKAGE = "com.pinelabs.masterapp";
        Intent intent = new Intent();
        intent.setAction(PLUTUS_SMART_ACTION);
        intent.setPackage(PLUTUS_SMART_PACKAGE);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Messenger mServerMessenger = new Messenger(service);
            Message message = Message.obtain(null, 1001);
            Bundle data = new Bundle();
            try {
                JSONObject object = new JSONObject();
                JSONObject Header = new JSONObject();
                JSONObject Detail = new JSONObject();
                String defaultAppId = "ca64099cf0a74e68af6aa4c9159283e5";
                if (context.getPackageName().equalsIgnoreCase("com.hotsoft.coffeeshop")) {
                    defaultAppId = "386e4aa24d324023b77efe76f607f0bb";
                }
                String finalAppId = (appId == null || appId.trim().isEmpty()) ? defaultAppId : appId;
                Header.put("ApplicationId", finalAppId);
                Toast.makeText(context, "PineLab ID Sent: " + finalAppId, Toast.LENGTH_LONG).show();
                Header.put("UserId", "dsoft");
                Header.put("VersionNo", "1.0");
                Detail.put("PrintRefNo", "123446779");
                Detail.put("SavePrintData", true);

                if (transactionType.equalsIgnoreCase("payment")) {
                    Header.put("MethodId", "1001");
                    Detail.put("TransactionType", "4001"); // Default to Card payment
                    Detail.put("BillingRefNo", generateTransactionId());
                    Detail.put("PaymentAmount", paymentAmount);
                } else {
                    Header.put("MethodId", "1002");
                    JSONArray arryData = new JSONArray();
                    JSONObject values = new JSONObject();
                    values.put("PrintDataType", 2);
                    values.put("PrinterWidth", 32);
                    values.put("IsCenterAligned", false);
                    values.put("ImageData", compressBitmap(bitmap));
                    arryData.put(values);
                    Detail.put("Data", arryData);
                }

                object.put("Header", Header);
                object.put("Detail", Detail);

                data.putString(BILLING_REQUEST_TAG, String.valueOf(object));
                message.setData(data);

                try {
                    message.replyTo = new Messenger(new IncomingHandler());
                    mServerMessenger.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(context, "Connection Error", Toast.LENGTH_SHORT).show();
        }
    };

    public class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String value = bundle.getString(BILLING_RESPONSE_TAG);
            JSONObject response = null;
            try {
                response = new JSONObject(value);
                JSONObject responseData = response.optJSONObject("Response");
                JSONObject paymentDetails = response.optJSONObject("Detail");
                if (pineLabDeviceResponse != null) {
                    pineLabDeviceResponse.deviceResponse(responseData);
                    pineLabDeviceResponse.paymentDetails(paymentDetails);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String compressBitmap(Bitmap bitmap) {
        Bitmap safeBitmap = bitmap.copy(Bitmap.Config.RGB_565, false);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        safeBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        return bytesToHex(outputStream.toByteArray());
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b & 0xFF));
        }
        return result.toString();
    }

    public static String generateTransactionId() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        return timestamp + "-" + uuid;
    }
}
