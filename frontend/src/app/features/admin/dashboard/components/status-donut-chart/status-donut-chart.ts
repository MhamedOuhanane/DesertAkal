import { DecimalPipe } from '@angular/common';
import { Component, computed, input } from '@angular/core';

interface DonutSegment {
    label: string;
    value: number;
    percent: number;
    color: string;
    offset: number;
}

@Component({
    selector: 'app-status-donut-chart',
    imports: [DecimalPipe],
    template: `
        <div class="card p-6 h-full">
            <h3 class="mb-6 text-base font-bold text-text-primary">Reservations by Status</h3>

            @if (total() === 0) {
                <div class="flex h-48 items-center justify-center text-sm text-text-disabled">
                    No reservation data yet.
                </div>
            } @else {
                <div class="flex flex-col items-center gap-6 sm:flex-row">
                    <div class="relative shrink-0">
                        <svg width="180" height="180" viewBox="0 0 180 180">
                            @for (seg of segments(); track seg.label) {
                                <circle
                                    cx="90"
                                    cy="90"
                                    r="70"
                                    fill="none"
                                    [attr.stroke]="seg.color"
                                    stroke-width="24"
                                    [attr.stroke-dasharray]="
                                        seg.percent * 4.398 + ' ' + (439.8 - seg.percent * 4.398)
                                    "
                                    [attr.stroke-dashoffset]="-(seg.offset * 4.398)"
                                    stroke-linecap="round"
                                    transform="rotate(-90 90 90)"
                                    class="transition-all duration-700 ease-out"
                                />
                            }
                        </svg>
                        <div class="absolute inset-0 flex flex-col items-center justify-center">
                            <span class="text-2xl font-bold text-text-primary">{{ total() }}</span>
                            <span
                                class="text-[10px] font-medium uppercase tracking-wider text-text-disabled"
                                >Total</span
                            >
                        </div>
                    </div>

                    <div class="flex-1 space-y-3">
                        @for (seg of segments(); track seg.label) {
                            <div class="flex items-center justify-between gap-3">
                                <div class="flex items-center gap-2.5">
                                    <span
                                        class="h-3 w-3 shrink-0 rounded-full"
                                        [style.background-color]="seg.color"
                                    ></span>
                                    <span class="text-sm text-text-secondary">{{ seg.label }}</span>
                                </div>
                                <div class="flex items-center gap-2">
                                    <span class="text-sm font-bold text-text-primary">{{
                                        seg.value
                                    }}</span>
                                    <span class="text-xs text-text-disabled"
                                        >({{ seg.percent | number: '1.0-1' }}%)</span
                                    >
                                </div>
                            </div>
                        }
                    </div>
                </div>
            }
        </div>
    `,
    styles: ``,
})
export class StatusDonutChart {
    data = input.required<Record<string, number>>();

    private statusColors: Record<string, string> = {
        CONFIRMED: '#10b981',
        PENDING: '#f59e0b',
        CANCELLED: '#ef4444',
        COMPLETED: '#3b82f6',
        REFUNDED: '#8b5cf6',
        NO_SHOW: '#6b7280',
    };

    total = computed(() => {
        const d = this.data();
        return Object.values(d).reduce((sum, v) => sum + v, 0);
    });

    segments = computed((): DonutSegment[] => {
        const d = this.data();
        const t = this.total();
        if (t === 0) return [];

        let offset = 0;
        return Object.entries(d)
            .sort(([, a], [, b]) => b - a)
            .map(([label, value]) => {
                const percent = (value / t) * 100;
                const segment: DonutSegment = {
                    label: this.formatLabel(label),
                    value,
                    percent,
                    color: this.statusColors[label] || '#9ca3af',
                    offset,
                };
                offset += percent;
                return segment;
            });
    });

    private formatLabel(status: string): string {
        return status
            .replace(/_/g, ' ')
            .toLowerCase()
            .replace(/^./, (c) => c.toUpperCase());
    }
}
