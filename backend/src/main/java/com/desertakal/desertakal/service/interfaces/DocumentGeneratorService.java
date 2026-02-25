package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.reservation.ReservationFindDTO;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public interface DocumentGeneratorService {
    byte[] generateQRCodeImage(@NonNull UUID reservationUuid);
    byte[] generateReservationPdf(@NonNull ReservationFindDTO dto, byte[] qrCodeImage);
    void generateConfirmationAssets(@NonNull UUID reservationUuid);
}
