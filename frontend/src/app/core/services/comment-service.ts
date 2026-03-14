import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/response.models';
import { Comment, CommentCreate, CommentUpdate } from '../models/comment.model';

@Injectable({ providedIn: 'root' })
export class CommentService {
    private http = inject(HttpClient);
    private baseUrl = `${environment.apiUrl}/comments`;

    create(data: CommentCreate): Observable<ApiResponse<Comment>> {
        return this.http.post<ApiResponse<Comment>>(this.baseUrl, data);
    }

    update(uuid: string, data: CommentUpdate): Observable<ApiResponse<Comment>> {
        return this.http.put<ApiResponse<Comment>>(`${this.baseUrl}/${uuid}`, data);
    }

    delete(uuid: string): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${uuid}`);
    }

    find(uuid: string): Observable<ApiResponse<Comment>> {
        return this.http.get<ApiResponse<Comment>>(`${this.baseUrl}/${uuid}`);
    }
}