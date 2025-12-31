package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.payment.PaymentDTO;
import com.desertakal.desertakal.model.dto.payment.PaymentFindDTO;
import com.desertakal.desertakal.model.entity.Payment;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ReservationMapper.class})
public interface PaymentMapper {

    @Named("toDto")
    @Mapping(source = "reservation.uuid", target = "reservationUuid")
    @Mapping(expression = "java(payment.getReservation() != null && payment.getReservation().getTourist() != null ? " +
            "payment.getReservation().getTourist().getFullName() : null)",
            target = "touristName")
    @Mapping(source = "reservation.tourist.photo", target = "touristPhoto")
    PaymentDTO toDto(Payment payment);

    @InheritConfiguration(name = "toDto")
    PaymentFindDTO toFindDto(Payment payment);

    @IterableMapping(qualifiedByName = "toDto")
    List<PaymentDTO> toDtos(List<Payment> payments);
}
