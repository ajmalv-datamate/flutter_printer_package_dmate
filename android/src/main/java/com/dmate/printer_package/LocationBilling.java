package com.dmate.printer_package;

public class LocationBilling {
    public String locationName;
    public String billAmount;
    public String taxableAmount;
    public String cashAmount;
    public String creditAmount;
    public String unsettledAmount;
    public String sgst;
    public String cgst;
    public String pax;
    public String apc;

    public String getApc() { return apc; }
    public void setApc(String apc) { this.apc = apc; }

    public String getBillAmount() { return billAmount; }
    public void setBillAmount(String billAmount) { this.billAmount = billAmount; }

    public String getCashAmount() { return cashAmount; }
    public void setCashAmount(String cashAmount) { this.cashAmount = cashAmount; }

    public String getCgst() { return cgst; }
    public void setCgst(String cgst) { this.cgst = cgst; }

    public String getCreditAmount() { return creditAmount; }
    public void setCreditAmount(String creditAmount) { this.creditAmount = creditAmount; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getPax() { return pax; }
    public void setPax(String pax) { this.pax = pax; }

    public String getSgst() { return sgst; }
    public void setSgst(String sgst) { this.sgst = sgst; }

    public String getTaxableAmount() { return taxableAmount; }
    public void setTaxableAmount(String taxableAmount) { this.taxableAmount = taxableAmount; }

    public String getUnsettledAmount() {
        return unsettledAmount == null || unsettledAmount.isEmpty() ? "0" : unsettledAmount;
    }
    public void setUnsettledAmount(String unsettledAmount) { this.unsettledAmount = unsettledAmount; }
}
