import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment.development';
import { Review, ReviewCreate, ReviewFilters, ReviewUpdate } from '../models/review.model';
import { Observable } from 'rxjs';
import { ApiResponse, Pagination } from '../models/response.models';
import { buildHttpParams } from '../utils/http-utils';

@Injectable({
    providedIn: 'root',
})
export class ReviewService {
    private http = inject(HttpClient);
    private baseUrl = `${environment.apiUrl}/reviews`;

    create(data: ReviewCreate): Observable<ApiResponse<Review>> {
        return this.http.post<ApiResponse<Review>>(this.baseUrl, data);
    }

    update(uuid: string, data: ReviewUpdate): Observable<ApiResponse<Review>> {
        return this.http.patch<ApiResponse<Review>>(`${this.baseUrl}/${uuid}`, data);
    }

    findAll(params: ReviewFilters): Observable<ApiResponse<Pagination<Review>>> {
        return this.http.get<ApiResponse<Pagination<Review>>>(this.baseUrl, {
            params: buildHttpParams(params),
        });
    }

    find(uuid: string): Observable<ApiResponse<Review>> {
        return this.http.get<ApiResponse<Review>>(`${this.baseUrl}/${uuid}`);
    }

    delete(uuid: string): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${uuid}`);
    }
}
