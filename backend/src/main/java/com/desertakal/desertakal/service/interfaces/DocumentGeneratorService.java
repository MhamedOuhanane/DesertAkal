package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.reservation.ReservationFindDTO;
import com.desertakal.desertakal.model.entity.Reservation;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public interface DocumentGeneratorService {
    void generateConfirmationAssets(@NonNull Reservation reservation);
    byte[] generateQRCodeImage(@NonNull UUID reservationUuid);
    byte[] generateReservationPdf(@NonNull ReservationFindDTO dto, byte[] qrCodeImage);
}
