package com.dmate.printer_package;

import java.util.List;

public class PrinterData {
    private String clientname="";
    private String locationName="";
    private String locationName1="";
    private String locationName2="";
    private String billNo="";
    private String date="";
    private String total="";
    private String sgst="";
    private String cgst="";
    private String totalPayable="";
    private String amountInWords="";
    private String paymentMode="";
    private String billedFor="";
    private String outlet="";
    private String gstIN="";
    private String fssai="";
    private String table="";
    private String waiter="";
    private String remarks="";
    private String parcel="";
    private String session="";
    private String sgstData="";
    private String sgstPer="";
    private String cgstData="";
    private String cgstPer="";
    private String cess="0.00";
    private String cessPer="0";
    private String pax="";
    private String kot="";
    private String email="";
    private String phone="";
    private String compliEmp="";
    private String compliAuth="";
    private String compliCompany="";
    private List<CartItem> cartItems;


    public PrinterData(String clientname,
                       String locationName,
                       String billNo,
                       String date,
                       String total,
                       String sgst,
                       String cgst,
                       String totalPayable,
                       String amountInWords,
                       String paymentMode,
                       String billedFor,
                       String outlet,
                       String gstIN,
                       String fssai,
                       String table,
                       String waiter,
                       String remarks,
                       String parcel,
                       String session,
                       String sgstData,
                        String sgstPer,
                        String cgstData,
                        String cgstPer,
                        String cess,
                        String cessPer,
                        String pax,
                        String compliEmp,
                        String compliAuth,
                        String compliCompany,
                       List<CartItem> cartItems) {
        this.clientname = clientname;
        this.locationName = locationName;
        this.billNo = billNo;
        this.date = date;
        this.total = total;
        this.sgst = sgst;
        this.cgst = cgst;
        this.totalPayable = totalPayable;
        this.amountInWords = amountInWords;
        this.paymentMode = paymentMode;
        this.billedFor = billedFor;
        this.outlet = outlet;
        this.gstIN = gstIN;
        this.fssai = fssai;
        this.table = table;
        this.waiter = waiter;
        this.remarks = remarks;
        this.parcel = parcel;
        this.sgstData = sgstData;
        this.sgstPer = sgstPer;
        this.cgstPer = cgstPer;
        this.cgstData = cgstData;
        this.cess = cess;
        this.cessPer = cessPer;
        this.session = session;
        this.pax = pax;
        this.compliEmp = compliEmp;
        this.compliAuth = compliAuth;
        this.compliCompany = compliCompany;
        this.cartItems = cartItems;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLocationName1() {
        return locationName1;
    }

    public void setLocationName1(String locationName1) {
        this.locationName1 = locationName1;
    }

    public String getLocationName2() {
        return locationName2;
    }

    public void setLocationName2(String locationName2) {
        this.locationName2 = locationName2;
    }

    public String getKot() {
        return kot;
    }

    public void setKot(String kot) {
        this.kot = kot;
    }

    public String getPax() {
        return pax;
    }

    public void setPax(String pax) {
        this.pax = pax;
    }

    public String getSgstData() {
        return sgstData;
    }

    public void setSgstData(String sgstData) {
        this.sgstData = sgstData;
    }

    public String getSgstPer() {
        return sgstPer;
    }

    public void setSgstPer(String sgstPer) {
        this.sgstPer = sgstPer;
    }

    public String getCgstData() {
        return cgstData;
    }

    public void setCgstData(String cgstData) {
        this.cgstData = cgstData;
    }

    public String getCgstPer() {
        return cgstPer;
    }

    public void setCgstPer(String cgstPer) {
        this.cgstPer = cgstPer;
    }

    public String getCess() {
        return cess;
    }

    public void setCess(String cess) {
        this.cess = cess;
    }

    public String getCessPer() {
        return cessPer;
    }

    public void setCessPer(String cessPer) {
        this.cessPer = cessPer;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getParcel() {
        return parcel;
    }

    public void setParcel(String parcel) {
        this.parcel = parcel;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getWaiter() {
        return waiter;
    }

    public void setWaiter(String waiter) {
        this.waiter = waiter;
    }

    public PrinterData() {}

    public String getClientname() {
        return clientname;
    }

    public void setClientname(String clientname) {
        this.clientname = clientname;
    }
    public String getGstIN() {
        return gstIN;
    }

    public void setGstIN(String gstIN) {
        this.gstIN = gstIN;
    }

    public String getFssai() {
        return fssai;
    }

    public void setFssai(String fssai) {
        this.fssai = fssai;
    }
    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getSgst() {
        return sgst;
    }

    public void setSgst(String sgst) {
        this.sgst = sgst;
    }

    public String getCgst() {
        return cgst;
    }

    public void setCgst(String cgst) {
        this.cgst = cgst;
    }

    public String getTotalPayable() {
        return totalPayable;
    }

    public void setTotalPayable(String totalPayable) {
        this.totalPayable = totalPayable;
    }

    public String getAmountInWords() {
        return amountInWords;
    }

    public void setAmountInWords(String amountInWords) {
        this.amountInWords = amountInWords;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getBilledFor() {
        return billedFor;
    }

    public void setBilledFor(String billedFor) {
        this.billedFor = billedFor;
    }

    public String getOutlet() {
        return outlet;
    }

    public void setOutlet(String outlet) {
        this.outlet = outlet;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public String getCompliAuth() {
        return compliAuth;
    }

    public void setCompliAuth(String compliAuth) {
        this.compliAuth = compliAuth;
    }

    public String getCompliCompany() {
        return compliCompany;
    }

    public void setCompliCompany(String compliCompany) {
        this.compliCompany = compliCompany;
    }

    public String getCompliEmp() {
        return compliEmp;
    }

    public void setCompliEmp(String compliEmp) {
        this.compliEmp = compliEmp;
    }

    public static class CartItem {
        private String itemName;
        private String quantity;
        private String price;
        private String total;

        public CartItem(String itemName, String quantity, String price, String total) {
            this.itemName = itemName;
            this.quantity = quantity;
            this.price = price;
            this.total = total;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public CartItem() {}

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public String getQuantity() {
            return quantity;
        }

        public void setQuantity(String quantity) {
            this.quantity = quantity;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }
    }
}
