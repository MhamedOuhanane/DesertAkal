import { PaymentMethod } from '../enums/payment-method.enum';
import { PaymentStatus } from '../enums/payment-status.enum';
import { PaymentType } from '../enums/payment-type.enum';
import { Reservation } from './reservation.model';

export interface Payment {
    readonly uuid: string;
    readonly date: string | Date;
    readonly amount: number;
    readonly status: PaymentStatus;
    readonly type: PaymentType;
    readonly method: PaymentMethod;
    readonly reservationUuid: string;
    readonly touristName: string;
    readonly touristPhoto: string;
}

export interface PaymentFind {
    readonly uuid: string;
    readonly date: string | Date;
    readonly amount: number;
    readonly status: PaymentStatus;
    readonly type: PaymentType;
    readonly method: PaymentMethod;
    readonly createdAt: string | Date;
    readonly reservation: Reservation;
}

export interface PaymentCreate {
    reservationUuid: string;
    method: PaymentMethod;
}

export interface PaymentResponse {
    readonly paymentUuid: string;
    readonly approvalUrl: string;
    readonly gatewayPaymentId: string;
    readonly method: PaymentMethod;
    readonly status: PaymentStatus;
}

export interface RefundRequest {
    amount: number;
}
