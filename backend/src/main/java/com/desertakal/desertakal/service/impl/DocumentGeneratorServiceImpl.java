package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.DocumentGenerationException;
import com.desertakal.desertakal.model.dto.reservation.ReservationFindDTO;
import com.desertakal.desertakal.model.entity.Reservation;
import com.desertakal.desertakal.model.mapper.ReservationMapper;
import com.desertakal.desertakal.service.interfaces.DocumentGeneratorService;
import com.desertakal.desertakal.service.interfaces.FileStorageService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DocumentGeneratorServiceImpl implements DocumentGeneratorService {
    private final FileStorageService fileStorageService;
    private final ReservationMapper reservationMapper;

    @Value("${app.frontend.url}")
    private String frontendBaseUrl;


    @Override
    @Transactional
    public void generateConfirmationAssets(@NonNull Reservation reservation) {
        log.info("Generating assets for Reservation: {}", reservation.getUuid());

        if (reservation.getPdfUrl() != null) fileStorageService.deleteFile(reservation.getPdfUrl());
        if (reservation.getQrCode() != null) fileStorageService.deleteFile(reservation.getQrCode());

        ReservationFindDTO dto = reservationMapper.toFindDto(reservation);
        byte[] qrCodeBytes = generateQRCodeImage(reservation.getUuid());
        byte[] pdfBytes = generateReservationPdf(dto, qrCodeBytes);

        long timestamp = System.currentTimeMillis();
        String pdfPath = "reservations/vouchers/" + reservation.getUuid() + "_" + timestamp + ".pdf";
        String qrPath = "reservations/qrcodes/" + reservation.getUuid() + "_" + timestamp + ".png";

        String finalPdfPath = fileStorageService.uploadBytes(pdfBytes, pdfPath, "application/pdf");
        String finalQrPath = fileStorageService.uploadBytes(qrCodeBytes, qrPath, "image/png");

        reservation.setPdfUrl(finalPdfPath);
        reservation.setQrCode(finalQrPath);

        log.info("Successfully updated reservation {} with new PDF and QR assets.", reservation.getUuid());
    }

    @Override
    public byte[] generateQRCodeImage(@NonNull UUID reservationUuid) {
        log.info("Generating QR Code for verification link...");
        try {
            String data = String.format("%s/reservations/verify/%s", frontendBaseUrl, reservationUuid);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("QR Generation failed: {}", e.getMessage());
            throw new DocumentGenerationException("Failed to generate QR Code image.");
        }
    }

    @Override
    public byte[] generateReservationPdf(@NonNull ReservationFindDTO dto, byte[] qrCodeImage) {
        log.info("Building PDF Document...");
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            byte[] logoBytes = fileStorageService.downloadFile("public/desert_akal_logo.png");

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                if (logoBytes != null) {
                    PDImageXObject pdLogo = PDImageXObject.createFromByteArray(document, logoBytes, "logo");
                    contentStream.drawImage(pdLogo, 50, 750, 100, 50);
                }

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 22);
                contentStream.newLineAtOffset(180, 765);
                contentStream.showText("RESERVATION VOUCHER");
                contentStream.endText();

                contentStream.setLineWidth(1f);
                contentStream.moveTo(50, 735);
                contentStream.lineTo(550, 735);
                contentStream.stroke();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.setLeading(20f);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("Reservation ID: " + dto.getUuid());
                contentStream.newLine();
                contentStream.showText("Customer: " + dto.getTouristName());
                contentStream.newLine();
                contentStream.showText("Tour: " + dto.getTourTitle());
                contentStream.newLine();
                contentStream.showText("Date: " + dto.getStartDate());
                contentStream.endText();

                PDImageXObject pdQr = PDImageXObject.createFromByteArray(document, qrCodeImage, "QR");
                contentStream.drawImage(pdQr, 400, 520, 130, 130);
            }

            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("PDF generation error: {}", e.getMessage());
            throw new DocumentGenerationException("Technical error: PDF voucher could not be generated.");
        }
    }
}