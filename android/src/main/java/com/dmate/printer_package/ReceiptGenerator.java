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
        int W = 48;
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
        sb.append("No  Item                 Qty   Rate     Amount").append("\n");
        sb.append(line(W)).append("\n");

        int index = 1;
        for (PrinterData.CartItem c : printerData.getCartItems()) {
            String rate = c.getPrice();
            double total = Double.parseDouble(rate) * Double.parseDouble(c.getQuantity());
            String quantityText = c.getQuantity();

            try {
                double qty = Double.parseDouble(c.getQuantity());
                quantityText = (qty % 1 == 0) ? String.valueOf((int) qty) : String.valueOf(qty);
            } catch (Exception e) {}

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
        sb.append(line(W)).append("\n");

        if (!printerData.getPaymentMode().equalsIgnoreCase("complimentary")) {
            int colWidth = 8;
            String[] taxHeaders = {"SGST%", "SGST", "CGST%", "CGST", "CESS%", "CESS"};
            for (String header : taxHeaders) {
                sb.append(center(header, colWidth));
            }
            sb.append("\n");

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

    public static String generateSalesReportText(String outlet, String fromDate, String toDate, List<String> list_rep, String total, boolean itemwise) {
        int W = 48;
        StringBuilder sb = new StringBuilder();

        sb.append(center(outlet, W)).append("\n");
        sb.append(center(itemwise ? "Item wise" : "Bill Wise", W)).append("\n");
        sb.append(center("From " + fromDate + " To " + toDate, W)).append("\n");
        sb.append(line(W)).append("\n");

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
        return padRight(c1, 20) + padLeft(c2, 8) + padLeft(c3, 10) + padLeft(c4, 10);
    }

    static String padRight(String text, int width) {
        if (text.length() > width) text = text.substring(0, width);
        return String.format("%-" + width + "s", text);
    }

    static String padLeft(String text, int width) {
        if (text.length() > width) text = text.substring(0, width);
        return String.format("%" + width + "s", text);
    }

    static String keyValue(String label, String value) {
        int labelWidth = 12;
        if (label.length() > labelWidth) label = label.substring(0, labelWidth);
        return String.format("%-" + labelWidth + "s : %s", label, value);
    }

    static String borderLine(int width) {
        return "+" + "-".repeat(width - 2) + "+";
    }

    static String borderText(String text, int width) {
        if (text.length() > width - 4) text = text.substring(0, width - 4);
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
            if (breakIndex == -1) breakIndex = width;
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
        int totalWidth = 45;
        int labelWidth = 20;
        int valueWidth = 8;
        String safeLabel = label.length() > labelWidth ? label.substring(0, labelWidth) : label;
        String block = String.format("%-" + labelWidth + "s : %" + valueWidth + "s", safeLabel, value);
        if (block.length() >= totalWidth) return block;
        return " ".repeat(totalWidth - block.length()) + block;
    }

    static String itemRow(String no, String name, String qty, String rate, String amount) {
        int noW = 3;
        int nameW = 20;
        int qtyW = 5;
        int rateW = 8;
        int amtW = 10;
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
            if (breakIndex == -1) breakIndex = width;
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

    private static void drawKeyValue(Canvas canvas, String key, String value, float keyX, float valueX, float y, float size) {
        Paint p = new Paint();
        p.setColor(Color.BLACK);
        p.setTextSize(size);
        p.setAntiAlias(false);
        canvas.drawText(key + " :", keyX, y, p);
        canvas.drawText(value, valueX, y, p);
    }

    public static Bitmap generateReceiptForPinelab(PrinterData printerData, Context context) {
        if (printerData.getRawText() != null && !printerData.getRawText().trim().isEmpty()) {
            return generateBitmapFromRawText(printerData.getRawText(), context);
        }
        PaperType paperType = getPaperType(context);
        final int width = (paperType == PaperType.MM_80) ? 576 : 384;
        int workingHeight = (paperType == PaperType.MM_80) ? 4500 : 3800;

        Bitmap bitmap = Bitmap.createBitmap(width, workingHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setDither(true);
        textPaint.setAntiAlias(false);
        textPaint.setSubpixelText(false);
        textPaint.setTypeface(Typeface.DEFAULT);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(1f);
        linePaint.setAntiAlias(false);

        float leftMargin = (paperType == PaperType.MM_80) ? 20f : 12f;
        float rightMargin = leftMargin;
        float contentWidth = width - leftMargin - rightMargin;

        float TXT_H1    = (paperType == PaperType.MM_80) ? 30f : 24f;
        float TXT_H2    = (paperType == PaperType.MM_80) ? 26f : 22f;
        float TXT_BODY  = (paperType == PaperType.MM_80) ? 24f : 20f;
        float TXT_SMALL = (paperType == PaperType.MM_80) ? 22f : 18f;
        float TXT_ITEM  = (paperType == PaperType.MM_80) ? 20f : 16f;

        float y = 20f;

        // HEADER
        y += drawMultilineText(canvas, printerData.getClientname(), width / 2f, y, TXT_H1, Paint.Align.CENTER, true, width);
        y += drawMultilineText(canvas, printerData.getLocationName() + " " + printerData.getLocationName1() + " " + printerData.getLocationName2(), width / 2f, y, TXT_SMALL, Paint.Align.CENTER, false, width);

        drawText(canvas, printerData.getEmail() + " , " + printerData.getPhone(), width / 2f, y += 25, TXT_SMALL, Paint.Align.CENTER, false);
        drawText(canvas, "GSTIN : " + printerData.getGstIN(), width / 2f, y += 25, TXT_SMALL, Paint.Align.CENTER, false);
        drawText(canvas, "FSSAI Lic No : " + printerData.getFssai(), width / 2f, y += 25, TXT_SMALL, Paint.Align.CENTER, false);

        y += 10;
        canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
        y += 25;

        // RECEIPT INFO
        float labelX = leftMargin;
        float valueX = labelX + ((paperType == PaperType.MM_80) ? 140f : 120f);

        drawKeyValue(canvas, "Bill No", printerData.getBillNo(), labelX, valueX, y, TXT_BODY); y += 25;
        drawKeyValue(canvas, "Date", printerData.getDate(), labelX, valueX, y, TXT_BODY); y += 25;
        drawKeyValue(canvas, "Table", printerData.getTable(), labelX, valueX, y, TXT_BODY); y += 25;
        drawKeyValue(canvas, "Waiter", printerData.getWaiter(), labelX, valueX, y, TXT_BODY); y += 20;

        canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
        y += 20;

        // ITEM TABLE
        float slW, itemW, rateW, qtyW, amtW;
        if (paperType == PaperType.MM_80) {
            slW = contentWidth * 0.10f;
            itemW = contentWidth * 0.40f;
            rateW = contentWidth * 0.20f;
            qtyW = contentWidth * 0.10f;
            amtW = contentWidth * 0.20f;
        } else {
            slW = contentWidth * 0.12f;
            itemW = contentWidth * 0.46f;
            rateW = contentWidth * 0.16f;
            qtyW = contentWidth * 0.10f;
            amtW = contentWidth * 0.16f;
        }

        float x = leftMargin;
        drawText(canvas, "Sl", x + slW / 2, y, TXT_SMALL, Paint.Align.CENTER, true); x += slW;
        drawText(canvas, "Dish", x + 2, y, TXT_SMALL, Paint.Align.LEFT, true); x += itemW;
        drawText(canvas, "Qty", x + rateW / 2, y, TXT_SMALL, Paint.Align.CENTER, true); x += rateW;
        drawText(canvas, "Rate", x + qtyW / 2, y, TXT_SMALL, Paint.Align.CENTER, true); x += qtyW;
        drawText(canvas, "Amt", x + amtW - 2, y, TXT_SMALL, Paint.Align.RIGHT, true);

        y += 15;
        canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
        y += 30;

        int sl = 1;
        int itemTextWidth = (int) itemW;

        for (PrinterData.CartItem item : printerData.getCartItems()) {
            double rate = Double.parseDouble(item.getPrice());
            double qty = Double.parseDouble(item.getQuantity());
            double total = rate * qty;

            x = leftMargin;
            drawText(canvas, String.valueOf(sl++), x + slW / 2, y, TXT_ITEM, Paint.Align.CENTER, false);
            x += slW;

            float h = drawMultilineText(canvas, item.getItemName(), x + 2, y, TXT_ITEM, Paint.Align.LEFT, false, itemTextWidth);
            x += itemW;

            drawText(canvas, item.getQuantity(), x + rateW / 2, y, TXT_ITEM, Paint.Align.CENTER, false);
            x += rateW;

            drawText(canvas, item.getPrice(), x + qtyW / 2, y, TXT_ITEM, Paint.Align.CENTER, false);
            x += qtyW;

            drawText(canvas, String.format("%.2f", total), x + amtW - 2, y, TXT_ITEM, Paint.Align.RIGHT, false);
            y += h + 8;
        }

        y += 10;
        canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
        y += 30;

        float labelTotalEndX = width - rightMargin - ((paperType == PaperType.MM_80) ? 120f : 90f);
        float valueTotalX = width - rightMargin;

        boolean isComplimentary = "complimentary".equalsIgnoreCase(printerData.getPaymentMode());

        if (!isComplimentary) {
            drawText(canvas, "Taxable Amount:", labelTotalEndX, y, TXT_BODY, Paint.Align.RIGHT, true);
            drawText(canvas, printerData.getTotal(), valueTotalX, y, TXT_BODY, Paint.Align.RIGHT, true);
            y += 35;

            drawText(canvas, "SGST:", labelTotalEndX, y, TXT_BODY, Paint.Align.RIGHT, true);
            drawText(canvas, printerData.getSgst(), valueTotalX, y, TXT_BODY, Paint.Align.RIGHT, true);
            y += 35;

            drawText(canvas, "CGST:", labelTotalEndX, y, TXT_BODY, Paint.Align.RIGHT, true);
            drawText(canvas, printerData.getCgst(), valueTotalX, y, TXT_BODY, Paint.Align.RIGHT, true);
            y += 35;

            drawText(canvas, "Total Amount Payable:", labelTotalEndX, y, TXT_SMALL, Paint.Align.RIGHT, true);
            drawText(canvas, printerData.getTotalPayable(), valueTotalX, y, TXT_SMALL, Paint.Align.RIGHT, true);
            y += 35;
        } else {
            try {
                double total = Double.parseDouble(printerData.getTotal());
                double roundedTotal = Math.round(total);
                double roundOffAmt = roundedTotal - total;

                drawText(canvas, "Round Off:", labelTotalEndX, y, TXT_BODY, Paint.Align.RIGHT, true);
                drawText(canvas, String.format("%.2f", roundOffAmt), valueTotalX, y, TXT_BODY, Paint.Align.RIGHT, true);
                y += 35;

                drawText(canvas, "Total Amount Payable:", labelTotalEndX, y, TXT_SMALL, Paint.Align.RIGHT, true);
                drawText(canvas, String.format("%.2f", roundedTotal), valueTotalX, y, TXT_SMALL, Paint.Align.RIGHT, true);
                y += 35;
            } catch (Exception e) {
                drawText(canvas, "Total Amount Payable:", labelTotalEndX, y, TXT_SMALL, Paint.Align.RIGHT, true);
                drawText(canvas, "0.00", valueTotalX, y, TXT_SMALL, Paint.Align.RIGHT, true);
                y += 35;
            }
        }

        canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
        y += 30;

        drawText(canvas, printerData.getAmountInWords(), leftMargin, y, TXT_SMALL, Paint.Align.LEFT, false);
        y += 30;

        if (!isComplimentary) {
            canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
            y += 20;

            float colWidth = contentWidth / 6f;
            float colX = leftMargin;

            String[] headers = {"SGST%", "SGST", "CGST%", "CGST", "CESS%", "CESS"};
            for (String h : headers) {
                drawText(canvas, h, colX + colWidth / 2, y, TXT_SMALL, Paint.Align.CENTER, true);
                colX += colWidth;
            }

            y += 25;
            colX = leftMargin;

            String[] values = {
                    printerData.getSgstPer(),
                    printerData.getSgstData(),
                    printerData.getCgstPer(),
                    printerData.getCgstData(),
                    printerData.getCessPer(),
                    printerData.getCess()
            };
            for (String v : values) {
                drawText(canvas, v, colX + colWidth / 2, y, TXT_ITEM, Paint.Align.CENTER, false);
                colX += colWidth;
            }
            y += 25;
        }

        canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
        y += 30;

        drawText(canvas, "Remarks: " + printerData.getRemarks(), leftMargin, y, TXT_ITEM, Paint.Align.LEFT, false);
        y += 25;

        drawText(canvas, "Pax: " + printerData.getPax(), leftMargin, y, TXT_ITEM, Paint.Align.LEFT, false);
        if (!TextUtils.isEmpty(printerData.getKot())) {
            drawText(canvas, "K.O.T : " + printerData.getKot(), width / 2f, y, TXT_ITEM, Paint.Align.CENTER, false);
        }
        y += 30;

        drawText(canvas, "Payment Mode : " + printerData.getPaymentMode(), leftMargin, y, TXT_ITEM, Paint.Align.LEFT, false);
        y += 30;

        drawText(canvas, "E.&O.E For " + printerData.getOutlet(), leftMargin, y, TXT_ITEM, Paint.Align.LEFT, false);
        y += 30;

        drawText(canvas, "Billed By " + printerData.getWaiter(), leftMargin, y, TXT_ITEM, Paint.Align.LEFT, false);
        y += 10;

        canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
        y += 30;

        drawText(canvas, "PARCEL ITEMS MUST BE USED WITHIN 2 HRS", width / 2f, y, TXT_SMALL, Paint.Align.CENTER, true);
        y += 40;

        drawText(canvas, "!!! THANK YOU VISIT AGAIN !!!", width / 2f, y, TXT_BODY, Paint.Align.CENTER, true);
        y += 30;

        float extraFeed = (paperType == PaperType.MM_80) ? 120f : 100f;
        y += extraFeed;

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
        if (bold) {
            p.setStyle(Paint.Style.FILL_AND_STROKE);
            p.setStrokeWidth(0.3f);
        }
        canvas.drawText(text, x, y, p);
    }

    private static float drawMultilineText(Canvas canvas, String text, float x, float y, float fontSize, Paint.Align align, boolean bold, float maxWidth) {
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
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (p.measureText(testLine) <= maxWidth) {
                currentLine = new StringBuilder(testLine);
            } else {
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

    private static float drawLocationFromSalesDetail(Canvas canvas, SalesSummaryDetail d, float y, int width, float left, float right) {
        float colDescX = left;
        float colColonX = 300f;
        float colAmountX = width - right;

        y = drawSectionTitle(canvas, d.getLocShName() != null ? d.getLocShName() : d.getLocName(), y, width);
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

        y = drawKVRow(canvas, "Net Sales", d.getBillAmt(), y, colDescX, colColonX, colAmountX);
        y += 30f;
        return y;
    }

    public static String generateShiftCloseReceipt(ShiftClosePrintData p) {
        final int W = 48;
        StringBuilder sb = new StringBuilder();

        sb.append(center(p.getLocation(), W)).append("\n");
        sb.append(line(W)).append("\n");
        sb.append(twoCol("Date :", p.getDate(), W)).append("\n");
        sb.append(twoCol("Time :", p.getTime(), W)).append("\n");
        sb.append(twoCol("Cash Book :", p.getCashbook(), W)).append("\n");
        sb.append(twoCol("User Name :", p.getUser(), W)).append("\n");
        sb.append(line(W)).append("\n");
        sb.append(twoCol("Opening Balance :", p.getOpeningbalance(), W)).append("\n");
        sb.append(twoCol("Cash Sales :", p.getCashSale(), W)).append("\n");
        sb.append(twoCol("Credit Sales :", p.getCreditSale(), W)).append("\n");
        sb.append(line(W)).append("\n");
        sb.append(twoCol("TOTAL SALES :", p.getTotalSale(), W)).append("\n");
        sb.append(line(W)).append("\n");
        sb.append(twoCol("Cash In Hand :", p.getCashInHand(), W)).append("\n");
        sb.append(twoCol("Cash Transfer :", p.getCashTransfer(), W)).append("\n");
        sb.append(twoCol("Cash Deposit :", p.getCashDeposit(), W)).append("\n");
        sb.append(twoCol("Cash Balance :", p.getCashBalance(), W)).append("\n");
        sb.append(twoCol("Payments :", p.getPayments(), W)).append("\n");
        sb.append(line(W)).append("\n");
        sb.append(line(W, '=')).append("\n");
        sb.append(twoCol("CLOSING AMOUNT :", p.getClosingAmt(), W)).append("\n");
        sb.append(line(W, '=')).append("\n");
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
        canvas.drawLine((width - w) / 2f, y + 6f, (width + w) / 2f, y + 6f, p);
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

    private static float drawKVRow(Canvas canvas, String label, String value, float y, float descX, float colonX, float amountX) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(26f);
        canvas.drawText(label, descX, y, p);
        canvas.drawText(":", colonX, y, p);
        p.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(value == null ? "" : value, amountX, y, p);
        p.setTextAlign(Paint.Align.LEFT);
        return y + 38f;
    }

    private static float drawCenterUnderlineText(Canvas canvas, String text, float y, float size, int width) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(size);
        p.setFakeBoldText(true);
        p.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(text, width / 2f, y, p);
        float w = p.measureText(text);
        canvas.drawLine((width - w) / 2f, y + 6f, (width + w) / 2f, y + 6f, p);
        return y + 30f;
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty() || s.equals("0") || s.equals("0.00");
    }

    private static float drawLocationSection(Canvas canvas, LocationBilling data, float y, int width, float leftMargin, float rightMargin) {
        float colDescX = leftMargin;
        float colColonX = 300f;
        float colAmountX = width - rightMargin;
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(2f);

        y = drawSectionTitle(canvas, data.locationName, y, width);
        y = drawTableHeader(canvas, y, colDescX, colAmountX);
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

        y = drawCenterUnderlineText(canvas, "Cover Turnover", y, 26f, width);
        y += 20f;
        y = drawKVRow(canvas, "Pax", data.pax, y, colDescX, colColonX, colAmountX);
        y = drawKVRow(canvas, "APC", data.apc, y, colDescX, colColonX, colAmountX);

        y += 20f;
        canvas.drawLine(leftMargin, y, width - rightMargin, y, linePaint);
        y += 30f;

        return y;
    }

    public static Bitmap generateSalesSummaryReceipt(String companyName, String dateRange, List<LocationBilling> locations) {
        final int width = 576;
        final int tempHeight = 15000;
        float y = 40f;
        float leftMargin = 20f;
        float rightMargin = 20f;

        Bitmap tempBitmap = Bitmap.createBitmap(width, tempHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(tempBitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);

        y += drawCenterText(canvas, companyName, y, 32f, true, width);
        y += 10f;
        y += drawCenterText(canvas, dateRange, y, 24f, false, width);
        y += 30f;
        canvas.drawLine(leftMargin, y, width - rightMargin, y, paint);
        y += 30f;

        for (LocationBilling location : locations) {
            y = drawLocationSection(canvas, location, y, width, leftMargin, rightMargin);
        }

        int finalHeight = Math.min((int) y, tempBitmap.getHeight());
        Bitmap finalBitmap = Bitmap.createBitmap(tempBitmap, 0, 0, width, finalHeight);
        tempBitmap.recycle();
        return finalBitmap;
    }

    private static String sanitizeText(String text) {
        if (text == null) return "";
        text = text.replaceAll("[^\\x00-\\x7F]", "");
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
