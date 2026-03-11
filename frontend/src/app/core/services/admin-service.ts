import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment.development';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/response.models';
import { AdminDashboard } from '../models/admin-dashboard.model';

@Injectable({
  providedIn: 'root',
})
export class AdminService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/admin`

    getDashboardStats(): Observable<ApiResponse<AdminDashboard>> {
        return this.http.get<ApiResponse<AdminDashboard>>(`${this.apiUrl}/dashboard/stats`);
    }
}
