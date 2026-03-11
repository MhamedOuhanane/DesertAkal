import { Component, input } from '@angular/core';

@Component({
    selector: 'app-stat-card',
    imports: [],
    template: `
        <div class="card hover-lift p-5">
            <div class="flex items-start justify-between">
                <div class="min-w-0 flex-1">
                    <p
                        class="truncate text-xs font-semibold uppercase tracking-wider text-text-tertiary"
                    >
                        {{ label() }}
                    </p>
                    <p class="mt-2 text-2xl font-bold text-text-primary sm:text-3xl">
                        {{ formattedValue() }}
                    </p>
                    @if (subtitle()) {
                        <p class="mt-1 text-xs text-text-secondary">
                            {{ subtitle() }}
                        </p>
                    }
                </div>
                <div
                    class="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl"
                    [class]="iconBgClass()"
                >
                    <ng-content />
                </div>
            </div>
        </div>
    `,
    styles: ``,
})
export class StatCard {
    label = input.required<string>();
    formattedValue = input.required<string>();
    subtitle = input<string>('');
    iconBgClass = input<string>('bg-primary/10 text-primary');
}
