package com.dmate.printer_package;

import org.json.JSONObject;

public interface PineLabDeviceResponse {
    void deviceResponse(JSONObject response);
    void paymentDetails(JSONObject paymentDetails);
}
