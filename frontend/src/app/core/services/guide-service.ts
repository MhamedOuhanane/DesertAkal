import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Guide, GuideCreate, GuideFilters, GuideFind, GuideUpdate } from '../models/guide.model';
import { ApiResponse, PageAble, Pagination } from '../models/response.models';
import { Tour } from '../models/tour.model';
import { Reservation, ReservationFilters } from '../models/reservation.model';
import { Review, ReviewFilters } from '../models/review.model';
import { buildHttpParams } from '../utils/http-utils';

@Injectable({ providedIn: 'root' })
export class GuideService {
    private http = inject(HttpClient);
    private baseUrl = `${environment.apiUrl}/guides`;

    create(dto: GuideCreate): Observable<ApiResponse<GuideFind>> {
        return this.http.post<ApiResponse<GuideFind>>(this.baseUrl, dto);
    }

    findOne(uuid: string): Observable<ApiResponse<GuideFind>> {
        return this.http.get<ApiResponse<GuideFind>>(`${this.baseUrl}/${uuid}`);
    }

    findAll(params: GuideFilters): Observable<ApiResponse<Pagination<Guide>>> {
        return this.http.get<ApiResponse<Pagination<Guide>>>(this.baseUrl, {
            params: buildHttpParams<GuideFilters>(params),
        });
    }

    update(uuid: string, dto: GuideUpdate): Observable<ApiResponse<GuideFind>> {
        return this.http.patch<ApiResponse<GuideFind>>(`${this.baseUrl}/${uuid}`, dto);
    }

    getTours(uuid: string, params: PageAble): Observable<ApiResponse<Pagination<Tour>>> {
        return this.http.get<ApiResponse<Pagination<Tour>>>(`${this.baseUrl}/${uuid}/tours`, {
            params: buildHttpParams<PageAble>(params),
        });
    }

    getReservations(
        uuid: string,
        params: ReservationFilters,
    ): Observable<ApiResponse<Pagination<Reservation>>> {
        return this.http.get<ApiResponse<Pagination<Reservation>>>(
            `${this.baseUrl}/${uuid}/reservations`,
            {
                params: buildHttpParams<ReservationFilters>(params),
            },
        );
    }

    getReviews(uuid: string, params: ReviewFilters): Observable<ApiResponse<Pagination<Review>>> {
        return this.http.get<ApiResponse<Pagination<Review>>>(`${this.baseUrl}/${uuid}/reviews`, {
            params: buildHttpParams<ReviewFilters>(params),
        });
    }
}
