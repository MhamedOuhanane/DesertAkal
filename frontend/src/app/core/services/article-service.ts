import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageAble, Pagination } from '../models/response.models';
import { Comment } from '../models/comment.model';
import { Reaction, ReactionFilters } from '../models/reaction.model';
import { Article, ArticleCreate, ArticleFilters, ArticleUpdate } from '../models/article.models';
import { buildHttpParams } from '../utils/http-utils';

@Injectable({ providedIn: 'root' })
export class ArticleService {
    private http = inject(HttpClient);
    private baseUrl = `${environment.apiUrl}/articles`;

    create(data: ArticleCreate, coverImage: File): Observable<ApiResponse<Article>> {
        const formData = new FormData;
        formData.append("article", new Blob([JSON.stringify(data)], { type: 'application.json'}));
        formData.append("coverImage",coverImage);
        return this.http.post<ApiResponse<Article>>(this.baseUrl, formData);
    }

    update(
        uuid: string, 
        data: ArticleUpdate,
        coverImage?: File
    ): Observable<ApiResponse<Article>> {
        const formData = new FormData();
        formData.append("article", new Blob([JSON.stringify(data)], { type: 'application/json' }));
        
        if (coverImage) {
            formData.append("coverImage", coverImage);
        }
        return this.http.put<ApiResponse<Article>>(`${this.baseUrl}/${uuid}`, formData);
    }

    updateImage(
        uuid: string,
        coverImage: File
    ): Observable<ApiResponse<Article>> {
        const formData = new FormData();
        formData.append("coverImage", coverImage);
        return this.http.put<ApiResponse<Article>>(`${this.baseUrl}/${uuid}/image`, formData);
    }

    findAll(params: ArticleFilters): Observable<ApiResponse<Pagination<Article>>> {
        return this.http.get<ApiResponse<Pagination<Article>>>(this.baseUrl, {
            params: buildHttpParams(params),
        });
    }

    delete(uuid: string): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${uuid}`);
    }

    getComments(uuid: string, params: PageAble): Observable<ApiResponse<Pagination<Comment>>> {
        return this.http.get<ApiResponse<Pagination<Comment>>>(`${this.baseUrl}/${uuid}/comments`, {
            params: buildHttpParams(params),
        });
    }

    getReactions(
        uuid: string,
        params: ReactionFilters,
    ): Observable<ApiResponse<Pagination<Reaction>>> {
        return this.http.get<ApiResponse<Pagination<Reaction>>>(
            `${this.baseUrl}/${uuid}/reactions`,
            { params: buildHttpParams(params) },
        );
    }
}
