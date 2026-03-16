import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, Pagination } from '../models/response.models';
import {
    Reservation,
    ReservationFind,
    ReservationFilters,
    ReservationVerification,
} from '../models/reservation.model';
import { Payment } from '../models/payment.model';
import { buildHttpParams } from '../utils/http-utils';

@Injectable({ providedIn: 'root' })
export class ReservationService {
    private http = inject(HttpClient);
    private baseUrl = `${environment.apiUrl}/reservations`;

    findAll(params: ReservationFilters): Observable<ApiResponse<Pagination<Reservation>>> {
        return this.http.get<ApiResponse<Pagination<Reservation>>>(this.baseUrl, {
            params: buildHttpParams(params),
        });
    }

    findOne(uuid: string): Observable<ApiResponse<ReservationFind>> {
        return this.http.get<ApiResponse<ReservationFind>>(`${this.baseUrl}/${uuid}`);
    }

    findByReference(reference: string): Observable<ApiResponse<ReservationFind>> {
        return this.http.get<ApiResponse<ReservationFind>>(`${this.baseUrl}/ref/${reference}`);
    }

    cancel(uuid: string): Observable<ApiResponse<void>> {
        return this.http.patch<ApiResponse<void>>(`${this.baseUrl}/${uuid}/cancel`, {});
    }

    delete(uuid: string): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${uuid}`);
    }

    verify(uuid: string): Observable<ApiResponse<ReservationVerification>> {
        return this.http.get<ApiResponse<ReservationVerification>>(
            `${this.baseUrl}/verify/${uuid}`,
        );
    }

    downloadPdf(uuid: string): Observable<Blob> {
        return this.http.get(`${this.baseUrl}/${uuid}/download`, {
            responseType: 'blob',
        });
    }

    getPayments(
        uuid: string,
        params: { page?: number; size?: number },
    ): Observable<ApiResponse<Pagination<Payment>>> {
        return this.http.get<ApiResponse<Pagination<Payment>>>(`${this.baseUrl}/${uuid}/payments`, {
            params: buildHttpParams(params),
        });
    }
}
