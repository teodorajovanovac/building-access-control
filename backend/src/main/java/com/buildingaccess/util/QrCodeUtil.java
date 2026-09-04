package com.buildingaccess.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/** Generiše QR kod on-the-fly, bez čuvanja slike u bazi — isti string uvek daje istu QR sliku. */
public final class QrCodeUtil {

    private static final int SIZE = 300;

    private QrCodeUtil() {
    }

    public static String generateBase64Png(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, SIZE, SIZE);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (WriterException | IOException e) {
            throw new IllegalStateException("Ne mogu da generišem QR kod", e);
        }
    }
}
