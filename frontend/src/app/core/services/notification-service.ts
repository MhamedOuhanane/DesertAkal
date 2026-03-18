    import { Injectable, inject } from '@angular/core';
    import { HttpClient, HttpParams } from '@angular/common/http';
    import { Observable } from 'rxjs';
    import { environment } from '../../../environments/environment';
    import { ApiResponse, PageAble, Pagination } from '../models/response.models';
    import { Notification, NotificationFind } from '../models/notification.model';
    import { buildHttpParams } from '../utils/http-utils';

    @Injectable({ providedIn: 'root' })
    export class NotificationService {
        private http = inject(HttpClient);
        private baseUrl = `${environment.apiUrl}/notifications`;

        findByUser(
            userUuid: string,
            params: PageAble,
        ): Observable<ApiResponse<Pagination<Notification>>> {
            return this.http.get<ApiResponse<Pagination<Notification>>>(
                `${this.baseUrl}/user/${userUuid}`,
                { params: buildHttpParams(params) },
            );
        }

        findOne(uuid: string): Observable<ApiResponse<NotificationFind>> {
            return this.http.get<ApiResponse<NotificationFind>>(`${this.baseUrl}/${uuid}`);
        }

        delete(uuid: string): Observable<ApiResponse<void>> {
            return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${uuid}`);
        }
    }
