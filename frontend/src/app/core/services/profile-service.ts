import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/response.models';
import { UserFind, UserUpdate } from '../models/user.models';
import { Tourist, TouristUpdate } from '../models/tourist.model';
import { GuideFind } from '../models/guide.model';

@Injectable({ providedIn: 'root' })
export class ProfileService {
    private http = inject(HttpClient);
    private api = environment.apiUrl;

    getUser(uuid: string): Observable<ApiResponse<UserFind>> {
        return this.http.get<ApiResponse<UserFind>>(`${this.api}/users/${uuid}`);
    }

    getTourist(uuid: string): Observable<ApiResponse<Tourist>> {
        return this.http.get<ApiResponse<Tourist>>(`${this.api}/tourists/${uuid}`);
    }

    getGuide(uuid: string): Observable<ApiResponse<GuideFind>> {
        return this.http.get<ApiResponse<GuideFind>>(`${this.api}/guides/${uuid}`);
    }

    updateUser(uuid: string, dto: UserUpdate): Observable<ApiResponse<UserFind>> {
        return this.http.patch<ApiResponse<UserFind>>(`${this.api}/users/${uuid}`, dto);
    }

    updateTourist(uuid: string, dto: TouristUpdate): Observable<ApiResponse<Tourist>> {
        return this.http.patch<ApiResponse<Tourist>>(`${this.api}/tourists/${uuid}`, dto);
    }

    updateGuide(uuid: string, dto: any): Observable<ApiResponse<GuideFind>> {
        return this.http.patch<ApiResponse<GuideFind>>(`${this.api}/guides/${uuid}`, dto);
    }

    updateUserPhoto(uuid: string, photo: File): Observable<ApiResponse<UserFind>> {
        const fd = new FormData();
        fd.append('photo', photo);
        return this.http.patch<ApiResponse<UserFind>>(`${this.api}/users/${uuid}/photo`, fd);
    }

    updateTouristAvatar(uuid: string, avatar: File): Observable<ApiResponse<Tourist>> {
        const fd = new FormData();
        fd.append('avatar', avatar);
        return this.http.patch<ApiResponse<Tourist>>(`${this.api}/tourists/${uuid}/avatar`, fd);
    }
}
