import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, Pagination } from '../models/response.models';
import {
    Permission,
    PermissionRequest,
    PermissionUpdate,
    PermissionFilters,
} from '../models/permission.model';
import { buildHttpParams } from '../utils/http-utils';

@Injectable({ providedIn: 'root' })
export class PermissionService {
    private http = inject(HttpClient);
    private baseUrl = `${environment.apiUrl}/permissions`;

    findAll(params: PermissionFilters): Observable<ApiResponse<Pagination<Permission>>> {
        return this.http.get<ApiResponse<Pagination<Permission>>>(this.baseUrl, {
            params: buildHttpParams(params),
        });
    }

    create(dto: PermissionRequest): Observable<ApiResponse<Permission>> {
        return this.http.post<ApiResponse<Permission>>(this.baseUrl, dto);
    }

    createBatch(dtos: PermissionRequest[]): Observable<ApiResponse<Permission[]>> {
        return this.http.post<ApiResponse<Permission[]>>(`${this.baseUrl}/batch`, dtos);
    }

    update(uuid: string, dto: PermissionUpdate): Observable<ApiResponse<Permission>> {
        return this.http.patch<ApiResponse<Permission>>(`${this.baseUrl}/${uuid}`, dto);
    }

    delete(uuid: string): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${uuid}`);
    }
}
