import { HttpParams } from '@angular/common/http';

export function buildHttpParams<T>(params: T): HttpParams {
    let httpParams = new HttpParams();
    if (!params) return httpParams;

    Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
            if (Array.isArray(value)) {
                value.forEach((v) => (httpParams = httpParams.append(key, v.toString())));
            } else {
                httpParams = httpParams.set(key, value.toString());
            }
        }
    });
    return httpParams;
}
