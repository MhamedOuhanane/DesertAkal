import { Component, computed, input } from "@angular/core";
import { MonthlyStats } from "../../../../../core/models/admin-dashboard.model";
import { CurrencyPipe, DecimalPipe } from "@angular/common";


@Component({
    selector: 'app-monthly-bar-chart',
    imports: [CurrencyPipe],
    template: `
    <div class="card p-6">
            <div class="mb-6 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                <h3 class="text-base font-bold text-text-primary">Monthly Performance</h3>
                <div class="flex items-center gap-4 text-xs text-text-secondary">
                    <span class="flex items-center gap-1.5">
                        <span class="h-2.5 w-2.5 rounded-full bg-primary"></span>
                        Revenue
                    </span>
                    <span class="flex items-center gap-1.5">
                        <span class="h-2.5 w-2.5 rounded-full bg-secondary"></span>
                        Reservations
                    </span>
                </div>
            </div>

            @if (chartData().length === 0) {
                <div class="flex h-48 items-center justify-center text-sm text-text-disabled">
                    No monthly data yet.
                </div>
            } @else {
                <div class="flex items-end gap-2 overflow-x-auto pb-2 sm:gap-3" style="min-height: 220px;">
                    @for (item of chartData(); track item.month) {
                        <div class="flex min-w-12 flex-1 flex-col items-center gap-1.5">
                            <div class="relative w-full" style="height: 180px;">
                                <div class="absolute bottom-0 left-0 right-0 flex justify-center gap-1">
                                    <div
                                        class="w-[40%] rounded-t-md bg-primary/80 transition-all duration-700 ease-out hover:bg-primary"
                                        [style.height.px]="item.revenueHeight"
                                        [title]="'$' + item.revenue.toLocaleString()"
                                    ></div>
                                    <div
                                        class="w-[40%] rounded-t-md bg-secondary/70 transition-all duration-700 ease-out hover:bg-secondary"
                                        [style.height.px]="item.countHeight"
                                        [title]="item.reservationCount + ' reservations'"
                                    ></div>
                                </div>
                            </div>

                            <span class="text-[10px] font-medium text-text-disabled">
                                {{ item.shortMonth }}
                            </span>
                        </div>
                    }
                </div>

                <div class="mt-6 grid grid-cols-2 gap-4 border-t border-divider pt-4 sm:grid-cols-3">
                    <div>
                        <p class="text-xs text-text-disabled">Total Revenue</p>
                        <p class="text-lg font-bold text-text-primary">
                            {{ totalRevenue() | currency: 'EUR' : 'symbol' : '1.0-0' }} 
                        </p>
                    </div>
                    <div>
                        <p class="text-xs text-text-disabled">Total Reservations</p>
                        <p class="text-lg font-bold text-text-primary">
                            {{ totalReservations() }}
                        </p>
                    </div>
                    <div class="hidden sm:block">
                        <p class="text-xs text-text-disabled">Avg/Month</p>
                        <p class="text-lg font-bold text-text-primary">
                            {{ avgRevenue() | currency: 'EUR' : 'symbol' : '1.0-0'}}
                        </p>
                    </div>
                </div>
            }
        </div>
    `,
})
export class MonthlyBarChart {
    data = input.required<MonthlyStats[]>();

    chartData = computed(() => {
        const d = this.data();
        if (!d.length) return [];

        const maxRevenue = Math.max(...d.map((m) => m.revenue), 1);
        const maxCount = Math.max(...d.map((m) => m.reservationCount), 1);
        const maxBarHeight = 160;

        return [...d]
            .sort((a, b) => a.month.localeCompare(b.month))
            .slice(-12)
            .map((m) => ({
                ...m,
                shortMonth: this.formatMonth(m.month),
                revenueHeight: Math.max((m.revenue / maxRevenue) * maxBarHeight, 4),
                countHeight: Math.max((m.reservationCount / maxCount) * maxBarHeight, 4),
            }));
    });

    totalRevenue = computed(() => this.data().reduce((s, m) => s + m.revenue, 0));

    totalReservations = computed(() => this.data().reduce((s, m) => s + m.reservationCount, 0));

    avgRevenue = computed(() => {
        const d = this.data();
        return d.length ? this.totalRevenue() / d.length : 0;
    });

    private formatMonth(month: string): string {
        const [year, m] = month.split('-');
        const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
        return months[parseInt(m, 10) - 1] || m;
    }
}