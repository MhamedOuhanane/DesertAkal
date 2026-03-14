import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageAble, Pagination } from '../models/response.models';
import { Comment } from '../models/comment.model';
import { Reaction, ReactionFilters } from '../models/reaction.model';
import { Article, ArticleFilters } from '../models/article.models';
import { buildHttpParams } from '../utils/http-utils';

@Injectable({ providedIn: 'root' })
export class ArticleService {
    private http = inject(HttpClient);
    private baseUrl = `${environment.apiUrl}/articles`;

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
