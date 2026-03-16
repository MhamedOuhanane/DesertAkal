import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { MatIcon } from '@angular/material/icon';
import { ReservationService } from '../../../core/services/reservation-service';
import { ReservationVerification } from '../../../core/models/reservation.model';
import { BrandLogo } from '../../../shared/components/brand-logo/brand-logo';
import { toast } from 'ngx-sonner';

type VerifyState = 'loading' | 'valid' | 'invalid' | 'error';

@Component({
    selector: 'app-reservation-verify',
    standalone: true,
    imports: [DatePipe, CurrencyPipe, MatIcon, RouterLink, BrandLogo],
    templateUrl: './reservation-verify.html',
})
export class ReservationVerify implements OnInit {
    private route = inject(ActivatedRoute);
    private reservationService = inject(ReservationService);

    state = signal<VerifyState>('loading');
    data = signal<ReservationVerification | null>(null);
    errorMessage = signal('');
    currentYear = new Date().getFullYear();

    statusConfig = computed(() => {
        const d = this.data();
        if (!d) return this.getStatusConfig('UNKNOWN');
        return this.getStatusConfig(d.status);
    });

    async ngOnInit(): Promise<void> {
        const uuid = this.route.snapshot.paramMap.get('uuid');
        if (!uuid) {
            this.state.set('error');
            this.errorMessage.set('No reservation ID provided.');
            return;
        }
        await this.verify(uuid);
    }

    private async verify(uuid: string): Promise<void> {
        this.state.set('loading');
        try {
            const res = await firstValueFrom(this.reservationService.verify(uuid));
            this.data.set(res.data!);
            console.log(res.data!);
            this.state.set(res.data!.valid ? 'valid' : 'invalid');
        } catch (err: any) {
            this.state.set('error');
            this.errorMessage.set(
                err?.error?.message || 'Unable to verify this reservation. It may not exist.',
            );
        }
    }

    getInitials(name: string): string {
        return (
            name
                ?.split(' ')
                .map((w) => w.charAt(0))
                .join('')
                .toUpperCase()
                .slice(0, 2) || '?'
        );
    }

    getStatusConfig(status: string): {
        bg: string;
        text: string;
        icon: string;
        label: string;
        ringColor: string;
    } {
        const map: Record<string, any> = {
            CONFIRMED: {
                bg: 'bg-green-500/10',
                text: 'text-green-600',
                icon: 'check_circle',
                label: 'Confirmed',
                ringColor: 'ring-green-500/30',
            },
            PENDING: {
                bg: 'bg-orange-500/10',
                text: 'text-orange-600',
                icon: 'schedule',
                label: 'Pending',
                ringColor: 'ring-orange-500/30',
            },
            CANCELLED: {
                bg: 'bg-red-500/10',
                text: 'text-red-600',
                icon: 'cancel',
                label: 'Cancelled',
                ringColor: 'ring-red-500/30',
            },
            COMPLETED: {
                bg: 'bg-blue-500/10',
                text: 'text-blue-600',
                icon: 'task_alt',
                label: 'Completed',
                ringColor: 'ring-blue-500/30',
            },
            REJECTED: {
                bg: 'bg-gray-500/10',
                text: 'text-gray-500',
                icon: 'do_not_disturb',
                label: 'Rejected',
                ringColor: 'ring-gray-500/30',
            },
        };
        return (
            map[status] || {
                bg: 'bg-gray-500/10',
                text: 'text-gray-500',
                icon: 'help',
                label: 'Unknown',
                ringColor: 'ring-gray-500/30',
            }
        );
    }

    getDaysDiff(): number {
        const d = this.data();
        if (!d) return 0;
        const start = new Date(d.startDate);
        const end = new Date(d.endDate);
        return Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
    }

    get isUpcoming(): boolean {
        const d = this.data();
        if (!d) return false;
        return new Date(d.startDate) > new Date();
    }

    get isOngoing(): boolean {
        const d = this.data();
        if (!d) return false;
        const now = new Date();
        return new Date(d.startDate) <= now && new Date(d.endDate) >= now;
    }
}
