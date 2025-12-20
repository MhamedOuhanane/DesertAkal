package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.payment.PaymentDTO;
import com.desertakal.desertakal.model.dto.payment.PaymentFindDTO;
import com.desertakal.desertakal.model.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ReservationMapper.class})
public interface PaymentMapper {

    @Mapping(source = "reservation.uuid", target = "reservationUuid")
    @Mapping(expression = "java(payment.getReservation() != null && payment.getReservation().getTourist() != null ? " +
            "payment.getReservation().getTourist().getFirstName() + \" \" + payment.getReservation().getTourist().getLastName() : null)",
            target = "touristName")
    @Mapping(source = "reservation.tourist.photo", target = "touristPhoto")
    PaymentDTO toDto(Payment payment);

    PaymentFindDTO toFindDto(Payment payment);

    List<PaymentDTO> toDtos(List<Payment> payments);
}
