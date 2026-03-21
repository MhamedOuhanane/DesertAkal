import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment.development';
import { ApiResponse, PageAble, Pagination } from '../models/response.models';
import { Observable } from 'rxjs';
import { buildHttpParams } from '../utils/http-utils';
import { Reservation, ReservationFilters } from '../models/reservation.model';
import { Payment, PaymentFilters } from '../models/payment.model';
import { Review, ReviewFilters } from '../models/review.model';
import { Tour } from '../models/tour.model';

@Injectable({
    providedIn: 'root',
})
export class TouristService {
    private readonly http = inject(HttpClient);
    private readonly apiUrl = `${environment.apiUrl}/tourists`;

    getTours(uuid: string, params: PageAble): Observable<ApiResponse<Pagination<Tour>>> {
        return this.http.get<ApiResponse<Pagination<Tour>>>(`${this.apiUrl}/${uuid}/tours`, {
            params: buildHttpParams(params),
        });
    }

    getReservations(
        uuid: string,
        params: ReservationFilters,
    ): Observable<ApiResponse<Pagination<Reservation>>> {
        return this.http.get<ApiResponse<Pagination<Reservation>>>(
            `${this.apiUrl}/${uuid}/reservations`,
            {
                params: buildHttpParams(params),
            },
        );
    }

    getPayments(
        uuid: string,
        params: PaymentFilters,
    ): Observable<ApiResponse<Pagination<Payment>>> {
        return this.http.get<ApiResponse<Pagination<Payment>>>(`${this.apiUrl}/${uuid}/payments`, {
            params: buildHttpParams(params),
        });
    }

    getReviews(uuid: string, params: ReviewFilters): Observable<ApiResponse<Pagination<Review>>> {
        return this.http.get<ApiResponse<Pagination<Review>>>(`${this.apiUrl}/${uuid}/reviews`, {
            params: buildHttpParams(params),
        });
    }
}
