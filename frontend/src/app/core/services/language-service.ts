import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, Pagination } from '../models/response.models';
import {
    Language,
    LanguageCreate,
    LanguageUpdate,
    LanguageFilters,
} from '../models/language.model';
import { buildHttpParams } from '../utils/http-utils';

@Injectable({ providedIn: 'root' })
export class LanguageService {
    private http = inject(HttpClient);
    private baseUrl = `${environment.apiUrl}/languages`;

    findAll(params: LanguageFilters): Observable<ApiResponse<Pagination<Language>>> {
        return this.http.get<ApiResponse<Pagination<Language>>>(this.baseUrl, {
            params: buildHttpParams(params),
        });
    }

    findOne(uuid: string): Observable<ApiResponse<Language>> {
        return this.http.get<ApiResponse<Language>>(`${this.baseUrl}/${uuid}`);
    }

    create(dto: LanguageCreate): Observable<ApiResponse<Language>> {
        return this.http.post<ApiResponse<Language>>(this.baseUrl, dto);
    }

    update(uuid: string, dto: LanguageUpdate): Observable<ApiResponse<Language>> {
        return this.http.patch<ApiResponse<Language>>(`${this.baseUrl}/${uuid}`, dto);
    }

    delete(uuid: string): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${uuid}`);
    }
}
