import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, Pagination, PageAble } from '../models/response.models';
import { Payment, PaymentFind, RefundRequest } from '../models/payment.model';
import { buildHttpParams } from '../utils/http-utils';

export interface PaymentFilters extends PageAble {
    status?: string;
    type?: string;
    method?: string;
}

@Injectable({ providedIn: 'root' })
export class PaymentService {
    private http = inject(HttpClient);
    private baseUrl = `${environment.apiUrl}/payments`;

    findAll(params: PaymentFilters): Observable<ApiResponse<Pagination<Payment>>> {
        return this.http.get<ApiResponse<Pagination<Payment>>>(this.baseUrl, {
            params: buildHttpParams(params),
        });
    }

    findOne(uuid: string): Observable<ApiResponse<PaymentFind>> {
        return this.http.get<ApiResponse<PaymentFind>>(`${this.baseUrl}/${uuid}`);
    }

    initiate(dto: any): Observable<ApiResponse<any>> {
        return this.http.post<ApiResponse<any>>(this.baseUrl, dto);
    }

    capture(orderId: string): Observable<ApiResponse<PaymentFind>> {
        return this.http.post<ApiResponse<PaymentFind>>(`${this.baseUrl}/capture`, {}, {
            params: new HttpParams().set('orderId', orderId)
        });
    }

    cancel(uuid: string): Observable<ApiResponse<PaymentFind>> {
        return this.http.post<ApiResponse<PaymentFind>>(`${this.baseUrl}/${uuid}/cancel`, {});
    }

    refund(uuid: string): Observable<ApiResponse<PaymentFind>> {
        return this.http.post<ApiResponse<PaymentFind>>(`${this.baseUrl}/${uuid}/refund`, {});
    }

    partialRefund(uuid: string, dto: RefundRequest): Observable<ApiResponse<PaymentFind>> {
        return this.http.post<ApiResponse<PaymentFind>>(`${this.baseUrl}/${uuid}/refund/partial`, dto);
    }
}