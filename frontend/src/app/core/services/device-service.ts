import { inject, Injectable } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root',
})
export class DeviceService {
    private readonly cookieService = inject(CookieService);
    private readonly DEVICE_ID_KEY = 'X-Device-ID';

    getDeviceId(): string {
        let deviceId = this.cookieService.get(this.DEVICE_ID_KEY);

        if (!deviceId) {
            deviceId = crypto.randomUUID();

            this.saveDeviceId(deviceId);
        }

        return deviceId;
    }

    private saveDeviceId(id: string) {
        const isSecure = environment.secureCookie;

        this.cookieService.set(this.DEVICE_ID_KEY, id, 365, '/', '', isSecure, 'Strict');
    }
}
