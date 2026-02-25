package com.desertakal.desertakal.service.impl;

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
        log.info("Generating PDF for Booking: {}", dto.getUuid());
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("DesertAkal - Reservation Voucher");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("Tourist: " + dto.getTouristName());
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Tour: " + dto.getTourTitle());
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Date: " + dto.getStartDate());
                contentStream.endText();

                PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, qrCodeImage, "QR_CODE");
                contentStream.drawImage(pdImage, 400, 650, 150, 150);
            }

            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("PDF Creation Error: {}", e.getMessage());
            throw new RuntimeException("Failed to build PDF");
        }
    }

    @Override
    public void generateConfirmationAssets(@NonNull UUID reservationUuid) {

    }
}
