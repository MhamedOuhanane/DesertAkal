import { Component, input } from '@angular/core';

export interface ActivityItem {
    icon: string;
    label: string;
    value: string;
    color: string;
}

@Component({
    selector: 'app-recent-activity',
    standalone: true,
    template: `
        <div class="card p-6">
            <h3 class="mb-5 text-base font-bold text-text-primary">Quick Overview</h3>
            <div class="space-y-4">
                @for (item of items(); track item.label) {
                    <div class="flex items-center gap-3">
                        <div
                            class="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl text-lg"
                            [class]="item.color"
                        >
                            {{ item.icon }}
                        </div>
                        <div class="min-w-0 flex-1">
                            <p class="truncate text-sm font-medium text-text-secondary">
                                {{ item.label }}
                            </p>
                        </div>
                        <span class="shrink-0 text-sm font-bold text-text-primary">
                            {{ item.value }}
                        </span>
                    </div>
                }
            </div>
        </div>
    `,
})
export class RecentActivity {
    items = input.required<ActivityItem[]>();
}
