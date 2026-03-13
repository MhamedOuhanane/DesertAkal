import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment.development';
import { Observable } from 'rxjs';
import { ApiResponse, Pagination } from '../models/response.models';
import { Language, LanguageFilters } from '../models/language.model';
import { buildHttpParams } from '../utils/http-utils';

@Injectable({
    providedIn: 'root',
})
export class LanguageService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/languages`;

    findAll(params: LanguageFilters): Observable<ApiResponse<Pagination<Language>>> {
        return this.http.get<ApiResponse<Pagination<Language>>>(this.apiUrl, {
            params: buildHttpParams(params),
        });
    }
}
