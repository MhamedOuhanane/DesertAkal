import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Guide, GuideCreate, GuideFilters, GuideFind, GuideUpdate } from '../models/guide.model';
import { ApiResponse, PageAble, Pagination } from '../models/response.models';
import { Tour } from '../models/tour.model';
import { Reservation, ReservationFilters } from '../models/reservation.model';
import { Review, ReviewFilters } from '../models/review.model';

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
        let httpParams = new HttpParams();

        if (params.search) httpParams = httpParams.set('search', params.search);
        if (params.language) httpParams = httpParams.set('language', params.language);
        httpParams = httpParams
            .set('page', (params.page ?? 0).toString())
            .set('size', (params.size ?? 10).toString())
            .set('sortBy', params.sortBy ?? 'lastLoginAt')
            .set('order', params.order ?? 'asc');

        return this.http.get<ApiResponse<Pagination<Guide>>>(this.baseUrl, {
            params: httpParams,
        });
    }

    update(uuid: string, dto: GuideUpdate): Observable<ApiResponse<GuideFind>> {
        return this.http.patch<ApiResponse<GuideFind>>(
            `${this.baseUrl}/${uuid}`,
            dto
        );
    }

    getTours(
        uuid: string,
        params: PageAble
    ): Observable<ApiResponse<Pagination<Tour>>> {
        let httpParams = new HttpParams();
        if (params.page !== undefined) httpParams = httpParams.set('page', params.page);
        if (params.size !== undefined) httpParams = httpParams.set('size', params.size);
        if (params.sortBy) httpParams = httpParams.set('sortBy', params.sortBy);
        if (params.order) httpParams = httpParams.set('order', params.order);

        return this.http.get<ApiResponse<Pagination<Tour>>>(
            `${this.baseUrl}/${uuid}/tours`,
            { params: httpParams }
        );
    }

    getReservations(uuid: string, params: ReservationFilters): Observable<ApiResponse<Pagination<Reservation>>> {
        let httpParams = new HttpParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                httpParams = httpParams.set(key, value);
            }
        });

        return this.http.get<ApiResponse<Pagination<Reservation>>>(
            `${this.baseUrl}/${uuid}/reservations`,
            { params: httpParams }
        );
    }

    getReviews(
        uuid: string,
        params: ReviewFilters
    ): Observable<ApiResponse<Pagination<Review>>> {
        let httpParams = new HttpParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                httpParams = httpParams.set(key, value);
            }
        });

        return this.http.get<ApiResponse<Pagination<Review>>>(
            `${this.baseUrl}/${uuid}/reviews`,
            { params: httpParams }
        );
    }
}