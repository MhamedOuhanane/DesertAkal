import { DecimalPipe } from '@angular/common';
import { Component, input } from '@angular/core';
import { MatIcon } from '@angular/material/icon';

@Component({
    selector: 'app-stats-card',
    standalone: true,
    imports: [MatIcon, DecimalPipe],
    template: `
        <div class="card p-5 transition-all hover:border-primary/20">
            <div class="flex items-center justify-between">
                <div>
                    <p class="text-xs font-medium text-text-tertiary">{{ label() }}</p>
                    <p class="mt-1 text-2xl font-bold text-text-primary">
                        @if (prefix()) {
                            {{ prefix() }}
                        }
                        {{ value() | number: format() }}
                        @if (suffix()) {
                            <span class="text-sm font-normal text-text-tertiary">{{
                                suffix()
                            }}</span>
                        }
                    </p>
                    @if (subtitle()) {
                        <p class="mt-0.5 text-[11px] text-text-tertiary">{{ subtitle() }}</p>
                    }
                </div>
                <div
                    class="flex h-12 w-12 items-center justify-center rounded-xl"
                    [class]="iconBg()"
                >
                    <mat-icon
                        [class]="iconColor()"
                        style="font-size: 22px; width: 22px; height: 22px"
                    >
                        {{ icon() }}
                    </mat-icon>
                </div>
            </div>
        </div>
    `,
})
export class StatsCard {
    label = input.required<string>();
    value = input.required<number>();
    icon = input('analytics');
    iconBg = input('bg-primary/10');
    iconColor = input('text-primary');
    prefix = input('');
    suffix = input('');
    subtitle = input('');
    format = input('1.0-0');
}
