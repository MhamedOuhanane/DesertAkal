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
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return new byte[0];
    }

    @Override
    public void generateConfirmationAssets(@NonNull UUID reservationUuid) {

    }
}
