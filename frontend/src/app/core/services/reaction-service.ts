import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment.development";
import { ReactionCreate, ReactionSummary, ReactionToggleResponse } from "../models/reaction.model";
import { ApiResponse } from "../models/response.models";
import { Observable } from "rxjs";

@Injectable({ providedIn: 'root' })
export class CommentService {
    private http = inject(HttpClient);
    private baseUrl = `${environment.apiUrl}/reactions`;

    toggle(data: ReactionCreate): Observable<ApiResponse<ReactionToggleResponse>> {
        return this.http.post<ApiResponse<ReactionToggleResponse>>(this.baseUrl, data);
    }

    getSummary(articleUuid: string): Observable<ApiResponse<ReactionSummary>> {
        return this.http.get<ApiResponse<ReactionSummary>>(`${this.baseUrl}/articles/${articleUuid}/summary`);
    }
}