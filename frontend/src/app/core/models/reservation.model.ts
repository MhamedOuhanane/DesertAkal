import { ResourceStatus } from "@angular/core";
import { ReservationStatus } from "../enums/reservation-status.enum";
import { Payment } from "./payment.model";

export interface Reservation {
    readonly uuid: string;
    readonly date: string | Date;
    readonly startDate: string | Date;
    readonly endDate: string | Date;
    readonly numberPeople: number;
    readonly amount: number;
    readonly status: ReservationStatus;
    readonly reference: string;
    readonly tourUuid: string;
    readonly tourTitle: string;
    readonly guideUuid: string;
    readonly guideName: string;
    readonly guidePhoto: string;
    readonly touristUuid: string;
    readonly touristName: string;
    readonly touristPhoto: string;
}

export interface ReservationFind extends Reservation{
    readonly qrCode: string; 
    readonly pdfUrl: string; 
    readonly createdAt: string | Date; 
    readonly updatedAt: string | Date;
    readonly payments: Payment;
}

export interface ReservationCreate {
    startDate: string | Date;
    numberPeople: number;
    amount: number;
    tourUuid: string;
    guideUuid: string;
}

export interface ReservationUpdate {
    startDate?: string | Date;
    numberPeople?: number;
    amount?: number;
    guideUuid?: string;
}

export interface ReservationVerification {
    readonly uuid: string;
    readonly touristName: string;
    readonly tourTitle: string;
    readonly startDate: string | Date;
    readonly numberPeople: number;
    readonly status: ReservationStatus;
    readonly reference: string;
    readonly isValid: boolean;
}