package com.desertakal.desertakal.service.interfaces;

import org.jspecify.annotations.NonNull;

import java.util.UUID;

public interface DocumentGeneratorService {
    byte[] generateQRCodeImage(@NonNull UUID reservationUuid);
    byte[] generateReservationPdf(@NonNull UUID reservaionUuid, byte @NonNull [] qrCodeImage);
    void generateReservationPdf(@NonNull UUID reservationUuid);
}
