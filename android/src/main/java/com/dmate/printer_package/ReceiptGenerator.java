package com.dmate.printer_package;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class ReceiptGenerator {

    // Configurable paper width for receipt rendering, defaults to 80mm (576 px)
    public static int paperWidth = 576;

    public static String generateReceipt(PrinterData printerData) {
        if (printerData.getRawText() != null && !printerData.getRawText().trim().isEmpty()) {
            return printerData.getRawText();
        }

        int W = (paperWidth >= 500) ? 48 : 32;
        StringBuilder sb = new StringBuilder();

        // HEADER
        sb.append("\n\n");
        sb.append(centerWrap(printerData.getLocationName() + " " + printerData.getLocationName1() + " " + printerData.getLocationName2(), W)).append("\n");

        sb.append(center(printerData.getEmail() + ", " + printerData.getPhone(), W)).append("\n");
        sb.append(center("GSTIN : " + printerData.getGstIN(), W)).append("\n");
        sb.append(center("FSSAI Lic No : " + printerData.getFssai(), W)).append("\n");

        sb.append(line(W)).append("\n");
        sb.append(center(printerData.getOutlet(), W)).append("\n");
        sb.append(line(W)).append("\n");

        // BILL DETAILS
        sb.append(keyValue("Bill No", printerData.getBillNo())).append("\n");
        sb.append(keyValue("Date", printerData.getDate())).append("\n");
        sb.append(keyValue("Table No", printerData.getTable())).append("\n");
        sb.append(keyValue("Waiter", printerData.getWaiter())).append("\n");

        sb.append(line(W)).append("\n");

        // ITEM HEADER
        if (paperWidth >= 500) {
            sb.append("No  Item                 Qty   Rate     Amount").append("\n");
        } else {
            sb.append("NoItem        Qty  Rate  Amount").append("\n");
        }
        sb.append(line(W)).append("\n");

        int index = 1;

        for (PrinterData.CartItem c : printerData.getCartItems()) {
            String rate = c.getPrice();
            double total = Double.parseDouble(rate) * Double.parseDouble(c.getQuantity());
            String quantityText = c.getQuantity();

            try {
                double qty = Double.parseDouble(c.getQuantity());
                quantityText = (qty % 1 == 0)
                        ? String.valueOf((int) qty)
                        : String.valueOf(qty);
            } catch (Exception e) {
                // ignore
            }

            sb.append(itemRow(
                    String.valueOf(index),
                    c.getItemName(),
                    quantityText,
                    rate,
                    String.format("%.2f", total)
            ));

            index++;
        }

        sb.append(line(W)).append("\n");

        // TOTALS
        sb.append(rightAlignedKeyColonValue("ITEM TOTAL", printerData.getTotal())).append("\n");

        sb.append(line(W, '=')).append("\n");
        if (!printerData.getPaymentMode().toLowerCase().equalsIgnoreCase("complimentary")) {
            sb.append(rightAlignedKeyColonValue("Taxable Amount", printerData.getTotal())).append("\n");
            sb.append(rightAlignedKeyColonValue("SGST", printerData.getSgst())).append("\n");
            sb.append(rightAlignedKeyColonValue("CGST", printerData.getCgst())).append("\n");
            sb.append(rightAlignedKeyColonValue("Total Amount Payable", printerData.getTotalPayable())).append("\n");
        } else {
            try {
                double total = Double.parseDouble(printerData.getTotal());
                double roundOffTotal = Math.round(total);
                double roundOffAmt = roundOffTotal - total;
                sb.append(rightAlignedKeyColonValue("Round Off", String.format("%.2f", roundOffAmt))).append("\n");
            } catch (Exception e) {
                sb.append(rightAlignedKeyColonValue("Round Off", String.format("%.2f", "0"))).append("\n");
            }
        }
        sb.append(line(W)).append("\n");

        sb.append(line(W, '=')).append("\n");

        sb.append(printerData.getAmountInWords()).append("\n");
        if (printerData.isShowGSTSplitting()) {
            sb.append(line(W)).append("\n");
            if (!printerData.getPaymentMode().equalsIgnoreCase("complimentary")) {
                int colWidth = (paperWidth >= 500) ? 8 : 5; // 48 / 6 columns
                String[] taxHeaders = {"SGST%", "SGST", "CGST%", "CGST", "CESS%", "CESS"};

                // Header row
                for (String header : taxHeaders) {
                    sb.append(center(header, colWidth));
                }
                sb.append("\n");

                // Values row
                String[] taxValues = {
                        printerData.getSgstPer(),
                        printerData.getSgstData(),
                        printerData.getCgstPer(),
                        printerData.getCgstData(),
                        printerData.getCessPer(),
                        printerData.getCess()
                };

                for (String value : taxValues) {
                    sb.append(center(value, colWidth));
                }

                sb.append("\n");
            }
        }
        sb.append(line(W)).append("\n");

        // FOOTER
        sb.append(keyValue("Remarks", printerData.getRemarks())).append("\n");
        sb.append(keyValue("Pax", printerData.getPax())).append("\n");

        if (!TextUtils.isEmpty(printerData.getKot().trim())) {
            sb.append(keyValue("KOT", printerData.getKot())).append("\n");
        }
        if (printerData.getPaymentMode().equalsIgnoreCase("complimentary")) {
            sb.append(keyValue("Auth. Person", printerData.getCompliAuth())).append("\n");
            sb.append(keyValue("Employee", printerData.getCompliEmp())).append("\n");
            sb.append(keyValue("Company", printerData.getCompliCompany())).append("\n");
        }
        sb.append(keyValue("Payment Mode", printerData.getPaymentMode())).append("\n");
        sb.append(keyValue("E.&O.E For", printerData.getOutlet())).append("\n");
        sb.append(keyValue("Billed By", printerData.getWaiter())).append("\n");

        sb.append("\n");

        sb.append(center("PARCEL ITEMS MUST BE USED WITHIN 2 HRS", W)).append("\n");
        sb.append(center("!! THANK YOU VISIT AGAIN !!", W)).append("\n");

        sb.append("\n\n\n");

        return sb.toString();
    }

    public static String generateSalesReportText(String outlet,
                                                 String fromDate,
                                                 String toDate,
                                                 List<String> list_rep,
                                                 String total,
                                                 boolean itemwise) {

        int W = (paperWidth >= 500) ? 48 : 32;
        StringBuilder sb = new StringBuilder();

        sb.append(center(outlet, W)).append("\n");
        sb.append(center(itemwise ? "Item wise" : "Bill Wise", W)).append("\n");
        sb.append(center("From " + fromDate + " To " + toDate, W)).append("\n");

        sb.append(line(W)).append("\n");

        // header
        if (itemwise) {
            sb.append(row4("Item", "Date", "Qty", "Value")).append("\n");
        } else {
            sb.append(row4("Bill#", "Date", "Amount", "User")).append("\n");
        }

        sb.append(line(W)).append("\n");

        for (String row : list_rep) {
            String col1 = row.substring(0, 100).trim();
            String col2 = row.substring(100, 200).trim();
            String col3 = row.substring(200, 300).trim();
            String col4 = row.substring(300, 400).trim();

            sb.append(row4(col1, col2, col3, col4)).append("\n");
        }

        sb.append(line(W)).append("\n");

        sb.append(center(total, 48)).append("\n");

        sb.append(line(W)).append("\n\n\n");

        return sb.toString();
    }

    static String row4(String c1, String c2, String c3, String c4) {
        int w1 = (paperWidth >= 500) ? 20 : 12;
        int w2 = (paperWidth >= 500) ? 8 : 6;
        int w3 = (paperWidth >= 500) ? 10 : 7;
        int w4 = (paperWidth >= 500) ? 10 : 7;
        return padRight(c1, w1)
                + padLeft(c2, w2)
                + padLeft(c3, w3)
                + padLeft(c4, w4);
    }

    static String padRight(String text, int width) {
        if (text.length() > width) {
            text = text.substring(0, width);
        }
        return String.format("%-" + width + "s", text);
    }

    static String padLeft(String text, int width) {
        if (text.length() > width) {
            text = text.substring(0, width);
        }
        return String.format("%" + width + "s", text);
    }

    static String keyValue(String label, String value) {
        int labelWidth = 12; // fixed width for labels

        if (label.length() > labelWidth) {
            label = label.substring(0, labelWidth);
        }

        return String.format("%-" + labelWidth + "s : %s", label, value);
    }

    static String borderLine(int width) {
        return "+" + "-".repeat(width - 2) + "+";
    }

    static String borderText(String text, int width) {
        if (text.length() > width - 4)
            text = text.substring(0, width - 4);

        int padding = width - 2 - text.length();
        return "|" + text + " ".repeat(padding) + "|";
    }

    static String wrapReceiptBorder(String receipt, int width) {
        StringBuilder result = new StringBuilder();

        result.append(borderLine(width)).append("\n");

        String[] lines = receipt.split("\n");

        for (String line : lines) {
            result.append(borderText(line, width)).append("\n");
        }

        result.append(borderLine(width));

        return result.toString();
    }

    static String centerWrap(String text, int width) {
        StringBuilder sb = new StringBuilder();

        while (text.length() > width) {
            int breakIndex = text.lastIndexOf(" ", width);

            if (breakIndex == -1) {
                breakIndex = width;
            }

            String line = text.substring(0, breakIndex).trim();
            sb.append(center(line, width)).append("\n");

            text = text.substring(breakIndex).trim();
        }

        sb.append(center(text, width));

        return sb.toString();
    }

    static String bigText(String text, int width) {
        return "\u001B!48" + center(text, width) + "\n\u001B!0";
    }

    static String center(String text, int width) {
        if (text.length() >= width) return text;

        int totalPadding = width - text.length();
        int leftPad = totalPadding / 2;
        int rightPad = totalPadding - leftPad;

        return " ".repeat(leftPad) + text + " ".repeat(rightPad);
    }

    static String line(int width) {
        return "-".repeat(width);
    }

    static String line(int width, char ch) {
        return String.valueOf(ch).repeat(width);
    }

    static String rightAlignedKeyColonValue(String label, String value) {
        int totalWidth = (paperWidth >= 500) ? 45 : 31;
        int labelWidth = (paperWidth >= 500) ? 20 : 12;
        int valueWidth = (paperWidth >= 500) ? 8 : 6;

        String safeLabel = label.length() > labelWidth
                ? label.substring(0, labelWidth)
                : label;

        String block = String.format(
                "%-" + labelWidth + "s : %" + valueWidth + "s",
                safeLabel,
                value
        );

        if (block.length() >= totalWidth) return block;

        return " ".repeat(totalWidth - block.length()) + block;
    }

    static String itemRow(
            String no,
            String name,
            String qty,
            String rate,
            String amount
    ) {
        int noW = (paperWidth >= 500) ? 3 : 2;
        int nameW = (paperWidth >= 500) ? 20 : 12;
        int qtyW = (paperWidth >= 500) ? 5 : 4;
        int rateW = (paperWidth >= 500) ? 8 : 6;
        int amtW = (paperWidth >= 500) ? 10 : 8;

        StringBuilder sb = new StringBuilder();

        List<String> lines = wrapText(name, nameW);

        for (int i = 0; i < lines.size(); i++) {
            sb.append(String.format(
                    "%-" + noW + "s%-" + nameW + "s%" + qtyW + "s%" + rateW + "s%" + amtW + "s",
                    i == 0 ? no : "",
                    lines.get(i),
                    i == 0 ? qty : "",
                    i == 0 ? rate : "",
                    i == 0 ? amount : ""
            ));

            sb.append("\n");
        }

        return sb.toString();
    }

    private static List<String> wrapText(String text, int width) {
        List<String> lines = new ArrayList<>();

        while (text.length() > width) {
            int breakIndex = text.lastIndexOf(" ", width);

            if (breakIndex == -1) {
                breakIndex = width;
            }

            lines.add(text.substring(0, breakIndex));
            text = text.substring(breakIndex).trim();
        }

        lines.add(text);
        return lines;
    }

    enum PaperType {
        MM_58,
        MM_80
    }

    private static PaperType getPaperType(Context context) {
        return paperWidth >= 500 ? PaperType.MM_80 : PaperType.MM_58;
    }

    private static int estimateReceiptHeight(PrinterData data, PaperType type) {
        int base = (type == PaperType.MM_80) ? 700 : 600;
        int perItem = (type == PaperType.MM_80) ? 70 : 60;
        return Math.min(base + (data.getCartItems().size() * perItem), 3000);
    }

    private static void drawKeyValue(
            Canvas canvas,
            String key,
            String value,
            float keyX,
            float valueX,
            float y,
            float size
    ) {
        Paint p = new Paint();
        p.setColor(Color.BLACK);
        p.setTextSize(size);
        p.setAntiAlias(false);

        canvas.drawText(key + " :", keyX, y, p);
        canvas.drawText(value, valueX, y, p);
    }

    public static Bitmap generateReceiptForPinelab(
            PrinterData printerData,
            Context context
    ) {
        if (printerData.getRawText() != null && !printerData.getRawText().trim().isEmpty()) {
            return generateBitmapFromRawText(printerData.getRawText(), context);
        }

        // ---------- PAPER TYPE ----------
        PaperType paperType = getPaperType(context);

        final int width = (paperType == PaperType.MM_80) ? 576 : 384;
        final float SCALE = 1.0f;

        int workingHeight = (paperType == PaperType.MM_80) ? 4500 : 3800;

        Bitmap bitmap = Bitmap.createBitmap(width, workingHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        // ---------- PAINTS ----------
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setDither(true);
        textPaint.setAntiAlias(false);
        textPaint.setSubpixelText(false);
        textPaint.setTypeface(Typeface.DEFAULT);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(2f);
        linePaint.setAntiAlias(false);

        // ---------- MARGINS ----------
        float leftMargin = (paperType == PaperType.MM_80) ? 20f : 12f;
        float rightMargin = leftMargin;
        float contentWidth = width - leftMargin - rightMargin;

        // ---------- FONT SIZES ----------
        float TXT_H1    = (paperType == PaperType.MM_80) ? 30f : 24f;
        float TXT_H2    = (paperType == PaperType.MM_80) ? 26f : 22f;
        float TXT_BODY  = (paperType == PaperType.MM_80) ? 24f : 20f;
        float TXT_SMALL = (paperType == PaperType.MM_80) ? 22f : 18f;
        float TXT_ITEM  = (paperType == PaperType.MM_80) ? 20f : 16f;

        float y = 20f;

        // ---------- HEADER ----------
        y += drawMultilineText(canvas, printerData.getClientname(),
                width / 2f, y, TXT_H1, Paint.Align.CENTER, true, width);

        y += drawMultilineText(canvas,
                printerData.getLocationName() + " " +
                        printerData.getLocationName1() + " " +
                        printerData.getLocationName2(),
                width / 2f, y, TXT_SMALL, Paint.Align.CENTER, false, width);

        drawText(canvas, printerData.getEmail() + " , " + printerData.getPhone(),
                width / 2f, y += 25, TXT_SMALL, Paint.Align.CENTER, false);

        drawText(canvas, "GSTIN : " + printerData.getGstIN(),
                width / 2f, y += 25, TXT_SMALL, Paint.Align.CENTER, false);

        drawText(canvas, "FSSAI Lic No : " + printerData.getFssai(),
                width / 2f, y += 25, TXT_SMALL, Paint.Align.CENTER, false);

        y += 14;
        canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
        y += 30;

        // ---------- RECEIPT INFO (fixed-colon alignment) ----------
        // All labels left, colons at a fixed x, values just right of colon
        float infoLabelX = leftMargin;
        float infoColonX = leftMargin + ((paperType == PaperType.MM_80) ? 130f : 110f);
        float infoValueX = infoColonX + 10f;

        Paint infoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        infoPaint.setTextSize(TXT_BODY);
        infoPaint.setColor(Color.BLACK);

        String[][] billRows = {
            {"Bill No",  printerData.getBillNo()},
            {"Date",     printerData.getDate()},
            {"Table No", printerData.getTable()},
            {"Waiter",   printerData.getWaiter()}
        };
        for (String[] row : billRows) {
            canvas.drawText(row[0], infoLabelX, y, infoPaint);
            canvas.drawText(":", infoColonX, y, infoPaint);
            canvas.drawText(row[1] != null ? row[1] : "", infoValueX, y, infoPaint);
            y += 28f;
        }
        y += 10f;

        canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
        y += 26;

        // ---------- ITEM TABLE HEADER ----------
        // Columns (left to right): No | Item | Qty | Rate | Amount
        float slW, itemW, qtyW, rateW, amtW;

        if (paperType == PaperType.MM_80) {
            slW   = contentWidth * 0.08f;
            itemW = contentWidth * 0.42f;
            qtyW  = contentWidth * 0.10f;
            rateW = contentWidth * 0.18f;
            amtW  = contentWidth * 0.22f;
        } else {
            slW   = contentWidth * 0.10f;
            itemW = contentWidth * 0.40f;
            qtyW  = contentWidth * 0.12f;
            rateW = contentWidth * 0.20f;
            amtW  = contentWidth * 0.18f;
        }

        // Compute column anchor X positions
        float xSl   = leftMargin;
        float xItem = xSl   + slW;
        float xQty  = xItem + itemW;
        float xRate = xQty  + qtyW;
        float xAmt  = xRate + rateW;

        drawText(canvas, "No",     xSl   + slW / 2f,   y, TXT_SMALL, Paint.Align.CENTER, true);
        drawText(canvas, "Item",   xItem + 2f,          y, TXT_SMALL, Paint.Align.LEFT,   true);
        drawText(canvas, "Qty",    xQty  + qtyW,        y, TXT_SMALL, Paint.Align.RIGHT,  true);
        drawText(canvas, "Rate",   xRate + rateW,       y, TXT_SMALL, Paint.Align.RIGHT,  true);
        drawText(canvas, "Amount", xAmt  + amtW - 2f,  y, TXT_SMALL, Paint.Align.RIGHT,  true);

        y += 22f;
        canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
        y += 28f;

        // ---------- ITEM ROWS ----------
        int sl = 1;
        int itemTextWidthPx = (int) itemW - 4;

        for (PrinterData.CartItem item : printerData.getCartItems()) {
            double rateVal, qtyVal;
            try { rateVal = Double.parseDouble(item.getPrice());    } catch (Exception e) { rateVal = 0; }
            try { qtyVal  = Double.parseDouble(item.getQuantity()); } catch (Exception e) { qtyVal  = 0; }
            double itemTotal = rateVal * qtyVal;

            String qtyStr  = (qtyVal % 1 == 0) ? String.valueOf((int) qtyVal) : String.valueOf(qtyVal);
            String rateStr = item.getPrice()    != null ? item.getPrice()    : "0.00";
            String amtStr  = String.format("%.2f", itemTotal);

            drawText(canvas, String.valueOf(sl++), xSl + slW / 2f, y, TXT_ITEM, Paint.Align.CENTER, false);

            float rowH = drawMultilineText(canvas, item.getItemName(),
                    xItem + 2f, y, TXT_ITEM, Paint.Align.LEFT, false, itemTextWidthPx);

            drawText(canvas, qtyStr,  xQty  + qtyW,       y, TXT_ITEM, Paint.Align.RIGHT, false);
            drawText(canvas, rateStr, xRate + rateW,       y, TXT_ITEM, Paint.Align.RIGHT, false);
            drawText(canvas, amtStr,  xAmt  + amtW - 2f,  y, TXT_ITEM, Paint.Align.RIGHT, false);

            y += Math.max(rowH, TXT_ITEM + 10f);
        }

        // ---------- ITEM TOTAL ----------
        y += 14f;
        canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
        y += 28f;

        float totLabelEndX = width - rightMargin - ((paperType == PaperType.MM_80) ? 145f : 100f);
        float totValueX    = width - rightMargin;

        Paint totPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        totPaint.setTextSize(TXT_BODY);
        totPaint.setColor(Color.BLACK);
        totPaint.setTextAlign(Paint.Align.RIGHT);
        totPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        totPaint.setStrokeWidth(0.4f);
        canvas.drawText("ITEM TOTAL :", totLabelEndX, y, totPaint);
        canvas.drawText(printerData.getTotal() != null ? printerData.getTotal() : "0.00", totValueX, y, totPaint);
        y += TXT_BODY + 16f;

        // ---------- TAX SUMMARY ----------
        canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
        y += 6f;
        canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
        y += 28f;

        boolean isComplimentary = "complimentary".equalsIgnoreCase(printerData.getPaymentMode());

        Paint taxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        taxPaint.setTextSize(TXT_BODY);
        taxPaint.setColor(Color.BLACK);
        taxPaint.setTextAlign(Paint.Align.RIGHT);

        if (!isComplimentary) {
            canvas.drawText("Taxable Amount :", totLabelEndX, y, taxPaint);
            canvas.drawText(printerData.getTotal() != null ? printerData.getTotal() : "0.00", totValueX, y, taxPaint);
            y += TXT_BODY + 10f;

            canvas.drawText("SGST :", totLabelEndX, y, taxPaint);
            canvas.drawText(printerData.getSgst() != null ? printerData.getSgst() : "0.00", totValueX, y, taxPaint);
            y += TXT_BODY + 10f;

            canvas.drawText("CGST :", totLabelEndX, y, taxPaint);
            canvas.drawText(printerData.getCgst() != null ? printerData.getCgst() : "0.00", totValueX, y, taxPaint);
            y += TXT_BODY + 18f;

            // Double line before Total Amount Payable (premium look)
            canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
            y += 6f;
            canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
            y += 26f;

            Paint payPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            payPaint.setTextSize(TXT_H2);
            payPaint.setColor(Color.BLACK);
            payPaint.setTextAlign(Paint.Align.RIGHT);
            payPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            payPaint.setStrokeWidth(0.6f);
            canvas.drawText("Total Amount Payable :", totLabelEndX, y, payPaint);
            canvas.drawText(printerData.getTotalPayable() != null ? printerData.getTotalPayable() : "0.00", totValueX, y, payPaint);
            y += TXT_H2 + 18f;
        } else {
            try {
                double totalAmt   = Double.parseDouble(printerData.getTotal());
                double roundedAmt = Math.round(totalAmt);
                double roundOff   = roundedAmt - totalAmt;

                canvas.drawText("Round Off :", totLabelEndX, y, taxPaint);
                canvas.drawText(String.format("%.2f", roundOff), totValueX, y, taxPaint);
                y += TXT_BODY + 18f;

                canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
                y += 6f;
                canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
                y += 26f;

                Paint payPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                payPaint.setTextSize(TXT_H2);
                payPaint.setColor(Color.BLACK);
                payPaint.setTextAlign(Paint.Align.RIGHT);
                payPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                payPaint.setStrokeWidth(0.6f);
                canvas.drawText("Total Amount Payable :", totLabelEndX, y, payPaint);
                canvas.drawText(String.format("%.2f", roundedAmt), totValueX, y, payPaint);
                y += TXT_H2 + 12f;
            } catch (Exception e) {
                canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
                y += 16f;
                Paint payPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                payPaint.setTextSize(TXT_H2);
                payPaint.setColor(Color.BLACK);
                payPaint.setTextAlign(Paint.Align.RIGHT);
                payPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                payPaint.setStrokeWidth(0.6f);
                canvas.drawText("Total Amount Payable :", totLabelEndX, y, payPaint);
                canvas.drawText("0.00", totValueX, y, payPaint);
                y += TXT_H2 + 12f;
            }
        }

        // ---------- AMOUNT IN WORDS ----------
        canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
        y += 24f;
        y += drawMultilineText(canvas, printerData.getAmountInWords(),
                leftMargin, y, TXT_SMALL, Paint.Align.LEFT, false, (int) contentWidth);
        y += 12f;

        // ---------- GST SPLIT TABLE (non-complimentary only) ----------
        if (!isComplimentary && printerData.isShowGSTSplitting()) {
            canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
            y += 22f;

            float colW = contentWidth / 6f;
            String[] hdr = {"SGST%", "SGST", "CGST%", "CGST", "CESS%", "CESS"};
            float cx = leftMargin;
            for (String h : hdr) {
                drawText(canvas, h, cx + colW / 2f, y, TXT_SMALL, Paint.Align.CENTER, true);
                cx += colW;
            }
            y += TXT_SMALL + 8f;

            String[] vals = {
                    printerData.getSgstPer(), printerData.getSgstData(),
                    printerData.getCgstPer(), printerData.getCgstData(),
                    printerData.getCessPer(), printerData.getCess()
            };
            cx = leftMargin;
            for (String v : vals) {
                drawText(canvas, v != null ? v : "0.00", cx + colW / 2f, y, TXT_ITEM, Paint.Align.CENTER, false);
                cx += colW;
            }
            y += TXT_ITEM + 8f;
        }

        // ---------- FOOTER ----------
        canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
        y += 24f;

        Paint footPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        footPaint.setTextSize(TXT_ITEM);
        footPaint.setColor(Color.BLACK);

        // Remarks (only if non-empty)
        if (!TextUtils.isEmpty(printerData.getRemarks())) {
            canvas.drawText("Remarks", infoLabelX, y, footPaint);
            canvas.drawText(":", infoColonX, y, footPaint);
            canvas.drawText(printerData.getRemarks(), infoValueX, y, footPaint);
            y += TXT_ITEM + 8f;
        }

        // Pax on its own line
        canvas.drawText("Pax", infoLabelX, y, footPaint);
        canvas.drawText(":", infoColonX, y, footPaint);
        canvas.drawText(printerData.getPax() != null ? printerData.getPax() : "", infoValueX, y, footPaint);
        y += TXT_ITEM + 8f;

        // KOT on its own line (was overlapping Pax before)
        if (!TextUtils.isEmpty(printerData.getKot()) && !printerData.getKot().trim().isEmpty()) {
            canvas.drawText("KOT", infoLabelX, y, footPaint);
            canvas.drawText(":", infoColonX, y, footPaint);
            canvas.drawText(printerData.getKot(), infoValueX, y, footPaint);
            y += TXT_ITEM + 8f;
        }

        if (isComplimentary) {
            String[][] compliRows = {
                {"Auth. Person", printerData.getCompliAuth()},
                {"Employee",     printerData.getCompliEmp()},
                {"Company",      printerData.getCompliCompany()}
            };
            for (String[] row : compliRows) {
                canvas.drawText(row[0], infoLabelX, y, footPaint);
                canvas.drawText(":", infoColonX, y, footPaint);
                canvas.drawText(row[1] != null ? row[1] : "", infoValueX, y, footPaint);
                y += TXT_ITEM + 8f;
            }
        }

        String[][] finalRows = {
            {"Payment Mode", printerData.getPaymentMode()},
            {"E.&O.E For",   printerData.getOutlet()},
            {"Billed By",    printerData.getWaiter()}
        };
        for (String[] row : finalRows) {
            canvas.drawText(row[0], infoLabelX, y, footPaint);
            canvas.drawText(":", infoColonX, y, footPaint);
            canvas.drawText(row[1] != null ? row[1] : "", infoValueX, y, footPaint);
            y += TXT_ITEM + 8f;
        }

        y += 10f;
        canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
        y += 28f;

        // ---------- FINAL MESSAGES ----------
        drawText(canvas, "PARCEL ITEMS MUST BE USED WITHIN 2 HRS",
                width / 2f, y, TXT_SMALL, Paint.Align.CENTER, true);
        y += TXT_SMALL + 12f;

        drawText(canvas, "*** THANK YOU  VISIT AGAIN ***",
                width / 2f, y, TXT_H2, Paint.Align.CENTER, true);
        y += TXT_H2 + 10f;

        canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
        y += 5f;
        canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);

        // ---------- EXTRA FEED FOR CUT ----------
        float extraFeed = (paperType == PaperType.MM_80) ? 120f : 100f;
        y += extraFeed;

        // ---------- FINAL CROP ----------
        int finalHeight = (int) Math.ceil(y + 20);
        Bitmap finalBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, finalHeight);
        bitmap.recycle();
        return finalBitmap;
    }

    private static void drawText(Canvas canvas, String text, float x, float y, float fontSize, Paint.Align align, boolean bold) {
        if (text == null) return;

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(fontSize);
        p.setColor(Color.BLACK);
        p.setTextAlign(align);

        // Optional subtle enhancement for thermal printers (prevents faded thin strokes)
        if (bold) {
            p.setStyle(Paint.Style.FILL_AND_STROKE);
            p.setStrokeWidth(0.3f); // subtle bold enhancement (not overbold)
        }

        canvas.drawText(text, x, y, p);
    }

    private static float drawMultilineText(Canvas canvas, String text, float x, float y,
                                           float fontSize, Paint.Align align, boolean bold,
                                           float maxWidth) {
        if (text == null || text.isEmpty()) return 0f;

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(fontSize);
        p.setColor(Color.BLACK);
        p.setTextAlign(align);
        p.setTypeface(Typeface.create(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL));

        if (bold) {
            p.setStyle(Paint.Style.FILL_AND_STROKE);
            p.setStrokeWidth(0.3f);
        } else {
            p.setStyle(Paint.Style.FILL);
        }

        float lineHeight = fontSize + 6f;
        List<String> lines = new ArrayList<>();

        String[] words = text.split(" "); // split only on spaces
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine;
            if (currentLine.length() == 0) {
                testLine = word;
            } else {
                testLine = currentLine + " " + word;
            }

            if (p.measureText(testLine) <= maxWidth) {
                currentLine = new StringBuilder(testLine);
            } else {
                // Line full, commit it and start a new one
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        float yPos = y;
        for (String line : lines) {
            canvas.drawText(line, x, yPos, p);
            yPos += lineHeight;
        }

        return lines.size() * lineHeight;
    }

    private static float drawLocationFromSalesDetail(
            Canvas canvas,
            SalesSummaryDetail d,
            float y,
            int width,
            float left,
            float right
    ) {
        float colDescX = left;
        float colColonX = 300f;
        float colAmountX = width - right;

        // ---- LOCATION NAME ----
        y = drawSectionTitle(canvas,
                d.getLocShName() != null ? d.getLocShName() : d.getLocName(),
                y,
                width
        );

        y = drawTableHeader(canvas, y, colDescX, colAmountX);

        y = drawKVRow(canvas, "Bill Amount", d.getBillAmt(), y, colDescX, colColonX, colAmountX);
        y = drawKVRow(canvas, "Discount Allowed", d.getDiscAmt(), y, colDescX, colColonX, colAmountX);
        y = drawKVRow(canvas, "Taxable Amount", d.getTaxableAmt(), y, colDescX, colColonX, colAmountX);
        y = drawKVRow(canvas, "Cash Amount", d.getCashAmt(), y, colDescX, colColonX, colAmountX);
        y = drawKVRow(canvas, "Credit Amount", d.getCreditAmt(), y, colDescX, colColonX, colAmountX);
        y = drawKVRow(canvas, "Un-Settled Amount", d.getUnsettled(), y, colDescX, colColonX, colAmountX);
        y = drawKVRow(canvas, "Un-Billed KOT", d.getUbKotAmt(), y, colDescX, colColonX, colAmountX);
        y = drawKVRow(canvas, "VAT", d.getSalesTax(), y, colDescX, colColonX, colAmountX);
        y = drawKVRow(canvas, "NC Bills", d.getNcBillAmt(), y, colDescX, colColonX, colAmountX);
        y = drawKVRow(canvas, "Complimentary Bills", d.getCompliAmt(), y, colDescX, colColonX, colAmountX);
        y = drawKVRow(canvas, "Parcel Charges", d.getnParcelAmt(), y, colDescX, colColonX, colAmountX);

        y += 10f;
        canvas.drawLine(left, y, width - right, y, new Paint());
        y += 30f;

        // ---- NET SALES ----
        y = drawKVRow(canvas, "Net Sales", d.getBillAmt(), y,
                colDescX, colColonX, colAmountX);

        y += 30f;
        return y;
    }

    public static String generateShiftCloseReceipt(ShiftClosePrintData p) {
        final int W = (paperWidth >= 500) ? 48 : 32;
        StringBuilder sb = new StringBuilder();

        // HEADER
        sb.append(center(p.getLocation(), W)).append("\n");

        sb.append(line(W)).append("\n");

        // BASIC INFO
        sb.append(twoCol("Date :", p.getDate(), W)).append("\n");
        sb.append(twoCol("Time :", p.getTime(), W)).append("\n");
        sb.append(twoCol("Cash Book :", p.getCashbook(), W)).append("\n");
        sb.append(twoCol("User Name :", p.getUser(), W)).append("\n");

        sb.append(line(W)).append("\n");

        // SALES DETAILS
        sb.append(twoCol("Opening Balance :", p.getOpeningbalance(), W)).append("\n");
        sb.append(twoCol("Cash Sales :", p.getCashSale(), W)).append("\n");
        sb.append(twoCol("Credit Sales :", p.getCreditSale(), W)).append("\n");

        sb.append(line(W)).append("\n");

        sb.append(twoCol("TOTAL SALES :", p.getTotalSale(), W)).append("\n");

        sb.append(line(W)).append("\n");

        // CASH DETAILS
        sb.append(twoCol("Cash In Hand :", p.getCashInHand(), W)).append("\n");
        sb.append(twoCol("Cash Transfer :", p.getCashTransfer(), W)).append("\n");
        sb.append(twoCol("Cash Deposit :", p.getCashDeposit(), W)).append("\n");
        sb.append(twoCol("Cash Balance :", p.getCashBalance(), W)).append("\n");
        sb.append(twoCol("Payments :", p.getPayments(), W)).append("\n");

        sb.append(line(W)).append("\n");

        // CLOSING AMOUNT
        sb.append(line(W, '=')).append("\n");
        sb.append(twoCol("CLOSING AMOUNT :", p.getClosingAmt(), W)).append("\n");
        sb.append(line(W, '=')).append("\n");

        // DENOMINATION
        sb.append(center("DENOMINATION DETAILS", W)).append("\n");
        sb.append(line(W)).append("\n");

        Object[][] denominationMap = {
                {2000, p.getDenominationDetails().getNDENO_2000()},
                {1000, p.getDenominationDetails().getNDENO_1000()},
                {500, p.getDenominationDetails().getNDENO_500()},
                {200, p.getDenominationDetails().getNDENO_200()},
                {100, p.getDenominationDetails().getNDENO_100()},
                {50, p.getDenominationDetails().getNDENO_50()},
                {20, p.getDenominationDetails().getNDENO_20()},
                {10, p.getDenominationDetails().getNDENO_10() + p.getDenominationDetails().getNDENO_COIN_10()},
                {5, p.getDenominationDetails().getNDENO_5() + p.getDenominationDetails().getNDENO_COIN_5()},
                {2, p.getDenominationDetails().getNDENO_2() + p.getDenominationDetails().getNDENO_COIN_2()},
                {1, p.getDenominationDetails().getNDENO_1() + p.getDenominationDetails().getNDENO_COIN_1()}
        };

        double totalAmount = 0;

        for (Object[] entry : denominationMap) {
            double value = ((Number) entry[0]).doubleValue();
            int count = (int) entry[1];

            double amount = value * count;
            totalAmount += amount;

            String left = "" + (int) value + " x " + count;
            String right = "" + String.format("%.2f", amount);

            sb.append(twoCol(left, right, W)).append("\n");
        }

        sb.append(line(W)).append("\n");

        sb.append(twoCol("TOTAL CASH :", "" + String.format("%.2f", totalAmount), W)).append("\n");

        sb.append(line(W)).append("\n\n\n");

        return sb.toString();
    }

    static String twoCol(String left, String right, int width) {
        int space = width - left.length() - right.length();

        if (space < 1) space = 1;

        return left + " ".repeat(space) + right;
    }

    private static float drawCenterText(Canvas c, String text, float y, float size, boolean bold, int width) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(size);
        p.setFakeBoldText(bold);
        p.setTextAlign(Paint.Align.CENTER);
        c.drawText(text, width / 2f, y, p);
        return size + 10f;
    }

    private static float drawSectionTitle(Canvas canvas, String title, float y, int width) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(30f);
        p.setFakeBoldText(true);
        p.setTextAlign(Paint.Align.CENTER);

        canvas.drawText(title, width / 2f, y, p);

        float w = p.measureText(title);
        canvas.drawLine(
                (width - w) / 2f,
                y + 6f,
                (width + w) / 2f,
                y + 6f,
                p
        );
        return y + 40f;
    }

    private static float drawTableHeader(Canvas canvas, float y, float leftX, float rightX) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(26f);
        p.setFakeBoldText(true);

        canvas.drawText("Description", leftX, y, p);
        p.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Amount", rightX, y, p);
        p.setTextAlign(Paint.Align.LEFT);

        return y + 30f;
    }

    private static float drawKVRow(
            Canvas canvas,
            String label,
            String value,
            float y,
            float descX,
            float colonX,
            float amountX
    ) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(26f);

        canvas.drawText(label, descX, y, p);
        canvas.drawText(":", colonX, y, p);

        p.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(value == null ? "" : value, amountX, y, p);
        p.setTextAlign(Paint.Align.LEFT);

        return y + 38f;
    }

    private static float drawCenterUnderlineText(
            Canvas canvas,
            String text,
            float y,
            float size,
            int width
    ) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(size);
        p.setFakeBoldText(true);
        p.setTextAlign(Paint.Align.CENTER);

        canvas.drawText(text, width / 2f, y, p);
        float w = p.measureText(text);

        canvas.drawLine(
                (width - w) / 2f,
                y + 6f,
                (width + w) / 2f,
                y + 6f,
                p
        );
        return y + 30f;
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty() || s.equals("0") || s.equals("0.00");
    }

    private static float drawLocationSection(
            Canvas canvas,
            LocationBilling data,
            float y,
            int width,
            float leftMargin,
            float rightMargin
    ) {
        float colDescX = leftMargin;
        float colColonX = 300f;
        float colAmountX = width - rightMargin;

        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(2f);

        // ---- Location Name ----
        y = drawSectionTitle(canvas, data.locationName, y, width);

        // ---- Header ----
        y = drawTableHeader(canvas, y, colDescX, colAmountX);

        // ---- Rows ----
        y = drawKVRow(canvas, "Bill Amount", data.billAmount, y, colDescX, colColonX, colAmountX);
        y = drawKVRow(canvas, "Taxable Amount", data.taxableAmount, y, colDescX, colColonX, colAmountX);

        if (!isEmpty(data.cashAmount))
            y = drawKVRow(canvas, "Cash Amount", data.cashAmount, y, colDescX, colColonX, colAmountX);

        if (!isEmpty(data.creditAmount))
            y = drawKVRow(canvas, "Credit Amount", data.creditAmount, y, colDescX, colColonX, colAmountX);

        y = drawKVRow(canvas, "Un-Settled Amount", data.unsettledAmount, y, colDescX, colColonX, colAmountX);
        y = drawKVRow(canvas, "SGST", data.sgst, y, colDescX, colColonX, colAmountX);
        y = drawKVRow(canvas, "CGST", data.cgst, y, colDescX, colColonX, colAmountX);

        y += 10f;
        canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
        y += 30f;

        // ---- Cover Turnover ----
        y = drawCenterUnderlineText(canvas, "Cover Turnover", y, 26f, width);

        y += 20f;
        y = drawKVRow(canvas, "Pax", data.pax, y, colDescX, colColonX, colAmountX);
        y = drawKVRow(canvas, "APC", data.apc, y, colDescX, colColonX, colAmountX);

        y += 20f;
        canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
        y += 30f;

        return y;
    }

    public static Bitmap generateSalesSummaryReceipt(
            String companyName,
            String dateRange,
            List<LocationBilling> locations
    ) {
        final int width = 576;
        final int tempHeight = 15000;

        float y = 40f;
        float leftMargin = 20f;
        float rightMargin = 20f;

        Bitmap tempBitmap = Bitmap.createBitmap(width, tempHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(tempBitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);

        // ---------- HEADER ----------
        y += drawCenterText(canvas, companyName, y, 32f, true, width);
        y += 10f;
        y += drawCenterText(canvas, dateRange, y, 24f, false, width);

        y += 30f;
        canvas.drawLine(leftMargin, y, width - rightMargin, y, paint);
        y += 30f;

        // ---------- LOCATIONS ----------
        for (LocationBilling location : locations) {
            y = drawLocationSection(
                    canvas,
                    location,
                    y,
                    width,
                    leftMargin,
                    rightMargin
            );
        }

        // ---------- FINAL CROP ----------
        int finalHeight = Math.min((int) y, tempBitmap.getHeight());

        Bitmap finalBitmap =
                Bitmap.createBitmap(tempBitmap, 0, 0, width, finalHeight);
        tempBitmap.recycle();

        return finalBitmap;
    }

    private static String sanitizeText(String text) {
        if (text == null) return "";

        // Remove unsupported characters (₹, emojis, etc.)
        text = text.replaceAll("[^\\x00-\\x7F]", "");

        // Normalize line endings
        text = text.replace("\r\n", "\n");

        return text;
    }

    public static Bitmap generateBitmapFromRawText(String text, Context context) {
        PaperType paperType = getPaperType(context);
        final int width = (paperType == PaperType.MM_80) ? 576 : 384;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setTextSize((paperType == PaperType.MM_80) ? 22f : 16f);
        paint.setTypeface(Typeface.MONOSPACE);

        String[] lines = text.split("\n");
        Paint.FontMetrics fm = paint.getFontMetrics();
        float lineHeight = fm.bottom - fm.top + 4;

        int height = (int) Math.ceil(lines.length * lineHeight + 40);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        float x = 20f;
        float y = -fm.top + 10f;

        for (String line : lines) {
            canvas.drawText(line, x, y, paint);
            y += lineHeight;
        }

        return bitmap;
    }
}
