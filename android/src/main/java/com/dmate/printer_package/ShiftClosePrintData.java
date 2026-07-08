package com.dmate.printer_package;

public class ShiftClosePrintData {
    private String companyName;
    private String location;
    private String date;
    private String time;
    private String cashbook;
    private String user;
    private String openingbalance;
    private String cashSale;
    private String creditSale;
    private String totalSale;
    private String closingAmt;
    private String cashInHand;
    private String cashTransfer;
    private String cashDeposit;
    private String cashBalance;
    private String payments;
    private DenominationDetails denominationDetails;

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getCashbook() { return cashbook; }
    public void setCashbook(String cashbook) { this.cashbook = cashbook; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getOpeningbalance() { return openingbalance; }
    public void setOpeningbalance(String openingbalance) { this.openingbalance = openingbalance; }

    public String getCashSale() { return cashSale; }
    public void setCashSale(String cashSale) { this.cashSale = cashSale; }

    public String getCreditSale() { return creditSale; }
    public void setCreditSale(String creditSale) { this.creditSale = creditSale; }

    public String getTotalSale() { return totalSale; }
    public void setTotalSale(String totalSale) { this.totalSale = totalSale; }

    public String getClosingAmt() { return closingAmt; }
    public void setClosingAmt(String closingAmt) { this.closingAmt = closingAmt; }

    public String getCashInHand() { return cashInHand; }
    public void setCashInHand(String cashInHand) { this.cashInHand = cashInHand; }

    public String getCashTransfer() { return cashTransfer; }
    public void setCashTransfer(String cashTransfer) { this.cashTransfer = cashTransfer; }

    public String getCashDeposit() { return cashDeposit; }
    public void setCashDeposit(String cashDeposit) { this.cashDeposit = cashDeposit; }

    public String getCashBalance() { return cashBalance; }
    public void setCashBalance(String cashBalance) { this.cashBalance = cashBalance; }

    public String getPayments() { return payments; }
    public void setPayments(String payments) { this.payments = payments; }

    public DenominationDetails getDenominationDetails() { return denominationDetails; }
    public void setDenominationDetails(DenominationDetails denominationDetails) {
        this.denominationDetails = denominationDetails;
    }
}
