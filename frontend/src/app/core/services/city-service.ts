import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment.development';
import { Observable } from 'rxjs';
import { ApiResponse, Pagination } from '../models/response.models';
import { City, CityCreate, CityFilters, CityFind, CityUpdate } from '../models/city.model';

@Injectable({
    providedIn: 'root',
})
export class CityService {
    private readonly http = inject(HttpClient);
    private readonly apiUrl = `${environment.apiUrl}/cities`;

    findAll(filters: CityFilters): Observable<ApiResponse<Pagination<City>>> {
        let params = new HttpParams();
        if (filters.search) params = params.set('search', filters.search);
        params = params
            .set('page', (filters.page ?? 0).toString())
            .set('size', (filters.size ?? 10).toString())
            .set('sortBy', filters.sortBy ?? 'name')
            .set('order', filters.order ?? 'asc');

        return this.http.get<ApiResponse<Pagination<City>>>(this.apiUrl, { params });
    }

    find(uuid: string): Observable<ApiResponse<CityFind>> {
        return this.http.get<ApiResponse<CityFind>>(`${this.apiUrl}/${uuid}`);
    }

    create(cityData: CityCreate): Observable<ApiResponse<CityFind>> {
        return this.http.post<ApiResponse<CityFind>>(this.apiUrl, cityData);
    }

    update(uuid: string, cityData: CityUpdate): Observable<ApiResponse<CityFind>> {
        return this.http.patch<ApiResponse<CityFind>>(`${this.apiUrl}/${uuid}`, cityData);
    }

    delete(uuid: string): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${uuid}`);
    }

    addImages(uuid: string, images: File[]): Observable<ApiResponse<City>> {
        const formData = new FormData();
        images.forEach((image) => formData.append('images', image));

        return this.http.post<ApiResponse<City>>(`${this.apiUrl}/${uuid}/images`, formData);
    }

    deleteImages(uuid: string, imageUuids: string[]): Observable<ApiResponse<void>> {
        return this.http.request<ApiResponse<void>>('delete', `${this.apiUrl}/${uuid}/images`, {
            body: imageUuids,
        });
    }

    setCoverImage(cityUuid: string, imageUuid: string): Observable<ApiResponse<void>> {
        return this.http.patch<ApiResponse<void>>(
            `${this.apiUrl}/${cityUuid}/images/${imageUuid}/set-cover`,
            {},
        );
    }
}
