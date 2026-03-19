import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, input, output } from '@angular/core';
import { MatIcon } from '@angular/material/icon';
import { Reservation } from '../../../core/models/reservation.model';

@Component({
    selector: 'app-reservation-card',
    imports: [DatePipe, CurrencyPipe, MatIcon],
    templateUrl: './reservation-card.html',
})
export class ReservationCard {
    reservation = input.required<Reservation>();
    showGuide = input(true);
    showTourist = input(false);
    showActions = input(true);
    showPrice = input(true);
    compact = input(false);

    viewClick = output<string>();
    cancelClick = output<string>();
    payClick = output<string>();
    downloadClick = output<string>();

    getStatusConfig(status: string): {
        bg: string;
        text: string;
        icon: string;
    } {
        const map: Record<string, any> = {
            CONFIRMED: { bg: 'bg-green-500/10', text: 'text-green-600', icon: 'check_circle' },
            PENDING: { bg: 'bg-orange-500/10', text: 'text-orange-600', icon: 'schedule' },
            CANCELLED: { bg: 'bg-red-500/10', text: 'text-red-600', icon: 'cancel' },
            COMPLETED: { bg: 'bg-blue-500/10', text: 'text-blue-600', icon: 'task_alt' },
            REJECTED: { bg: 'bg-gray-500/10', text: 'text-gray-500', icon: 'do_not_disturb' },
        };
        return map[status] || { bg: 'bg-gray-500/10', text: 'text-gray-500', icon: 'help' };
    }

    get canCancel(): boolean {
        const s = this.reservation().status;
        return s === 'PENDING' || s === 'CONFIRMED';
    }

    get canPay(): boolean {
        return this.reservation().status === 'PENDING';
    }
}
