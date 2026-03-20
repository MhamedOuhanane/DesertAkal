import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment.development';
import { Tour, TourCreate, TourFilters, TourFind, TourUpdate } from '../models/tour.model';
import { Observable } from 'rxjs';
import { ApiResponse, Pagination } from '../models/response.models';
import { buildHttpParams } from '../utils/http-utils';
import { City } from '../models/city.model';
import { Review, ReviewFilters } from '../models/review.model';

@Injectable({
    providedIn: 'root',
})
export class TourService {
    private readonly http = inject(HttpClient);
    private readonly apiUrl = `${environment.apiUrl}/tours`;

    findAll(filters: TourFilters): Observable<ApiResponse<Pagination<Tour>>> {
        return this.http.get<ApiResponse<Pagination<Tour>>>(this.apiUrl, {
            params: buildHttpParams(filters),
        });
    }

    findOne(uuid: string): Observable<ApiResponse<TourFind>> {
        return this.http.get<ApiResponse<TourFind>>(`${this.apiUrl}/${uuid}`);
    }

    create(data: TourCreate, image: File): Observable<ApiResponse<TourFind>> {
        const formData = new FormData();
        formData.append('tour', new Blob([JSON.stringify(data)], { type: 'application/json' }));
        formData.append('image', image);
        return this.http.post<ApiResponse<TourFind>>(this.apiUrl, formData);
    }

    update(uuid: string, data: TourUpdate): Observable<ApiResponse<TourFind>> {
        return this.http.patch<ApiResponse<TourFind>>(`${this.apiUrl}/${uuid}`, data);
    }

    updateImage(uuid: string, image: File): Observable<ApiResponse<TourFind>> {
        const formData = new FormData();
        formData.append('image', image);
        return this.http.patch<ApiResponse<TourFind>>(`${this.apiUrl}/${uuid}/image`, formData);
    }

    delete(uuid: string): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${uuid}`);
    }

    getCities(uuid: string): Observable<ApiResponse<City[]>> {
        return this.http.get<ApiResponse<City[]>>(`${this.apiUrl}/${uuid}/cities`)
    }

    getTop5Tours(): Observable<ApiResponse<Tour[]>> {
        return this.http.get<ApiResponse<Tour[]>>(`${this.apiUrl}/top5`)
    }
    
    getReviews(uuid:string, params: ReviewFilters): Observable<ApiResponse<Pagination<Review>>> {
        return this.http.get<ApiResponse<Pagination<Review>>>(`${this.apiUrl}/${uuid}/reviews`, {
            params: buildHttpParams(params),
        });
    }
}
