import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { Router } from '@angular/router';
import { Reservation } from '../../../core/models/reservation.model';
import { AuthStore } from '../../../core/auth/auth.store';
import { GuideService } from '../../../core/services/guide-service';

interface CalendarDay {
    date: Date;
    isCurrentMonth: boolean;
    isToday: boolean;
    assignments: Reservation[];
}

@Component({
    selector: 'app-my-schedule',
    imports: [DatePipe, MatIcon],
    templateUrl: './my-schedule.html',
})
export class MySchedule implements OnInit {
    private authStore = inject(AuthStore);
    private guideService = inject(GuideService);
    private router = inject(Router);

    userUuid = computed(() => this.authStore.user()?.uuid || '');

    isLoading = signal(true);
    currentDate = signal(new Date());
    allAssignments = signal<Reservation[]>([]);
    selectedDay = signal<CalendarDay | null>(null);

    readonly weekDays = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

    calendarDays = computed<CalendarDay[]>(() => {
        const current = this.currentDate();
        const year = current.getFullYear();
        const month = current.getMonth();

        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);

        let startDay = firstDay.getDay() - 1;
        if (startDay < 0) startDay = 6;

        const days: CalendarDay[] = [];
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        for (let i = startDay - 1; i >= 0; i--) {
            const date = new Date(year, month, -i);
            days.push({
                date,
                isCurrentMonth: false,
                isToday: false,
                assignments: this.getAssignmentsForDate(date),
            });
        }

        for (let d = 1; d <= lastDay.getDate(); d++) {
            const date = new Date(year, month, d);
            days.push({
                date,
                isCurrentMonth: true,
                isToday: date.getTime() === today.getTime(),
                assignments: this.getAssignmentsForDate(date),
            });
        }

        const remaining = 42 - days.length;
        for (let i = 1; i <= remaining; i++) {
            const date = new Date(year, month + 1, i);
            days.push({
                date,
                isCurrentMonth: false,
                isToday: false,
                assignments: this.getAssignmentsForDate(date),
            });
        }

        return days;
    });

    activeDaysCount = computed(() => {
        return this.calendarDays().filter(
            (day) => day.isCurrentMonth && day.assignments?.length > 0,
        ).length;
    });

    currentMonthLabel = computed(() => {
        const d = this.currentDate();
        return d.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
    });

    async ngOnInit(): Promise<void> {
        await this.loadAssignments();
    }

    private async loadAssignments(): Promise<void> {
        this.isLoading.set(true);
        try {
            const res = await firstValueFrom(
                this.guideService.getReservations(this.userUuid(), {
                    page: 0,
                    size: 100,
                    status: 'CONFIRMED',
                    sortBy: 'startDate',
                    order: 'asc',
                }),
            );
            this.allAssignments.set((res.data?.content as Reservation[]) || []);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to load schedule');
        } finally {
            this.isLoading.set(false);
        }
    }

    private getAssignmentsForDate(date: Date): Reservation[] {
        const dateStr = date.toISOString().split('T')[0];
        return this.allAssignments().filter((a) => {
            const start = new Date(a.startDate).toISOString().split('T')[0];
            const end = new Date(a.endDate).toISOString().split('T')[0];
            return dateStr >= start && dateStr <= end;
        });
    }

    previousMonth(): void {
        const d = this.currentDate();
        this.currentDate.set(new Date(d.getFullYear(), d.getMonth() - 1, 1));
    }

    nextMonth(): void {
        const d = this.currentDate();
        this.currentDate.set(new Date(d.getFullYear(), d.getMonth() + 1, 1));
    }

    goToToday(): void {
        this.currentDate.set(new Date());
    }

    selectDay(day: CalendarDay): void {
        if (day.assignments.length > 0) {
            this.selectedDay.set(day);
        }
    }

    viewAssignment(uuid: string): void {
        this.router.navigate(['/guide/dashboard/assignments', uuid]);
    }

    closeDetail(): void {
        this.selectedDay.set(null);
    }
}
