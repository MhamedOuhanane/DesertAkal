import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User, UserFilters, UserFind, UserUpdate } from '../models/user.models';
import { ApiResponse, PageAble, Pagination } from '../models/response.models';
import { buildHttpParams } from '../utils/http-utils';
import { Article, ArticleFilters } from '../models/article.models';
import { Comment } from '../models/comment.model';

@Injectable({ providedIn: 'root' })
export class UserService {
    private http = inject(HttpClient);
    private baseUrl = `${environment.apiUrl}/users`;

    findAll(params: UserFilters): Observable<ApiResponse<Pagination<User>>> {
        return this.http.get<ApiResponse<Pagination<User>>>(this.baseUrl, {
            params: buildHttpParams<UserFilters>(params),
        });
    }

    findOne(uuid: string): Observable<ApiResponse<UserFind>> {
        return this.http.get<ApiResponse<UserFind>>(`${this.baseUrl}/${uuid}`);
    }

    update(uuid: string, dto: UserUpdate): Observable<ApiResponse<UserFind>> {
        return this.http.patch<ApiResponse<UserFind>>(`${this.baseUrl}/${uuid}`, dto);
    }

    updateStatus(uuid: string, status: string): Observable<ApiResponse<UserFind>> {
        return this.http.patch<ApiResponse<UserFind>>(`${this.baseUrl}/${uuid}/status`, { status });
    }

    updatePhoto(uuid: string, photo: File): Observable<ApiResponse<UserFind>> {
        const formData = new FormData();
        formData.append('photo', photo);
        return this.http.patch<ApiResponse<UserFind>>(`${this.baseUrl}/${uuid}/photo`, formData);
    }

    delete(uuid: string): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${uuid}`);
    }

    getArticles(
        uuid: string,
        params: ArticleFilters,
    ): Observable<ApiResponse<Pagination<Article>>> {
        return this.http.get<ApiResponse<Pagination<Article>>>(`${this.baseUrl}/${uuid}/articles`, {
            params: buildHttpParams(params),
        });
    }

    getComments(uuid: string, params: PageAble): Observable<ApiResponse<Pagination<Comment>>> {
        return this.http.get<ApiResponse<Pagination<Comment>>>(`${this.baseUrl}/${uuid}/comments`, {
            params: buildHttpParams(params),
        });
    }
}
