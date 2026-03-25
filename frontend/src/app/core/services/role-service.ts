import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, Pagination } from '../models/response.models';
import { Role, RoleFind, RoleCreate, RoleUpdate, RoleFilters } from '../models/role.model';
import { buildHttpParams } from '../utils/http-utils';

@Injectable({ providedIn: 'root' })
export class RoleService {
    private http = inject(HttpClient);
    private baseUrl = `${environment.apiUrl}/roles`;

    findAll(params: RoleFilters): Observable<ApiResponse<Pagination<Role>>> {
        return this.http.get<ApiResponse<Pagination<Role>>>(this.baseUrl, {
            params: buildHttpParams(params),
        });
    }

    findOne(uuid: string): Observable<ApiResponse<RoleFind>> {
        return this.http.get<ApiResponse<RoleFind>>(`${this.baseUrl}/${uuid}`);
    }

    findByName(name: string): Observable<ApiResponse<RoleFind>> {
        return this.http.get<ApiResponse<RoleFind>>(`${this.baseUrl}/name/${name}`);
    }

    create(dto: RoleCreate): Observable<ApiResponse<RoleFind>> {
        return this.http.post<ApiResponse<RoleFind>>(this.baseUrl, dto);
    }

    update(uuid: string, dto: RoleUpdate): Observable<ApiResponse<RoleFind>> {
        return this.http.patch<ApiResponse<RoleFind>>(`${this.baseUrl}/${uuid}`, dto);
    }

    delete(uuid: string): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${uuid}`);
    }

    getPermissions(
        params: { search?: string; roleName: string } & RoleFilters,
    ): Observable<ApiResponse<Pagination<any>>> {
        return this.http.get<ApiResponse<Pagination<any>>>(`${this.baseUrl}/permissions`, {
            params: buildHttpParams(params),
        });
    }
}
