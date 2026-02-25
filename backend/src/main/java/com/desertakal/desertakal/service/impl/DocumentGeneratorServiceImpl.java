package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.DocumentGenerationException;
import com.desertakal.desertakal.model.dto.reservation.ReservationFindDTO;
import com.desertakal.desertakal.repository.ReservationRepository;
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
    private final ReservationRepository reservationRepository;

    @Override
    public byte[] generateQRCodeImage(@NonNull UUID reservationUuid) {
        log.info("Generating QR Code for reservation: {}", reservationUuid);
        try {
            String data = "reservations/qrCodes" + reservationUuid;
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();
        } catch (Exception e) {
            log.error("QR Generation Error: {}", e.getMessage());
            throw new RuntimeException("Could not generate QR Code");
        }
    }

    @Override
    public byte[] generateReservationPdf(@NonNull ReservationFindDTO dto, byte[] qrCodeImage) {
        log.info("Generating PDF Voucher for Reservation: {}", dto.getUuid());

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
                contentStream.showText("Customer Name: " + dto.getTouristName());
                contentStream.newLine();
                contentStream.showText("Tour Title: " + dto.getTourTitle());
                contentStream.newLine();
                contentStream.showText("Start Date: " + dto.getStartDate());
                contentStream.newLine();
                contentStream.showText("Number of People: " + dto.getNumberPeople());
                contentStream.endText();

                PDImageXObject pdQrCode = PDImageXObject.createFromByteArray(document, qrCodeImage, "QR_CODE");
                contentStream.drawImage(pdQrCode, 400, 520, 130, 130);

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
                contentStream.newLineAtOffset(415, 505);
                contentStream.showText("Scan to verify booking");
                contentStream.endText();
            }

            document.save(out);
            log.info("Successfully generated PDF voucher with logo for: {}", dto.getUuid());
            return out.toByteArray();

        } catch (IOException e) {
            log.error("Failed to build PDF document for {}: {}", dto.getUuid(), e.getMessage());
            throw new DocumentGenerationException("Technical error: Unable to generate PDF voucher.");
        }
    }

    @Override
    public void generateConfirmationAssets(@NonNull UUID reservationUuid) {

    }
}
