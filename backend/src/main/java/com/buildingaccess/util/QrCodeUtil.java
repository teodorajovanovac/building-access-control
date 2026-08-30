package com.buildingaccess.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Generiše QR kod on-the-fly iz trajnog stringa (badgeCode / gate pass code).
 * Nema čuvanja slike u bazi — isti ulazni string uvek daje identičnu QR sliku,
 * pa je ovo dovoljno za "trajni" QR kod stanara/osoblja/propusnice.
 */
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
