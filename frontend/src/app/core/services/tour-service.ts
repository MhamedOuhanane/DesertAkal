import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment.development';
import { Tour, TourCreate, TourFilters, TourFind, TourUpdate } from '../models/tour.model';
import { Observable } from 'rxjs';
import { ApiResponse, Pagination } from '../models/response.models';

@Injectable({
  providedIn: 'root',
})
export class TourService {
    private readonly http = inject(HttpClient);
    private readonly apiUrl = `${environment.apiUrl}/tours`;

    findAll(filters: TourFilters ): Observable<ApiResponse<Pagination<Tour>>> {
        let params = new HttpParams;
        if (filters.search) params = params.set("search", filters.search); 
        if (filters.city) params = params.set("city", filters.city); 
        if (filters.durationStr) params = params.set("durationStr", filters.durationStr); 
        if (filters.minRating) params = params.set("minRating", filters.minRating.toString()); 
        params = params.set('page', (filters.page ?? 0).toString())
            .set('size', (filters.size ?? 10).toString())
            .set('sortBy', filters.sortBy ?? 'createdAt')
            .set('order', filters.order ?? 'desc');

        return this.http.get<ApiResponse<Pagination<Tour>>>(this.apiUrl, { params });
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
}
