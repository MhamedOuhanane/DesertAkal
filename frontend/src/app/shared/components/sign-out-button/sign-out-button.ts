import { Component, output } from '@angular/core';
import { MatIcon } from '@angular/material/icon';

@Component({
    selector: 'app-sign-out-button',
    imports: [MatIcon],
    template: `
        <button
            (click)="onSignOut.emit()"
            class="group flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium text-error transition-all duration-200 hover:bg-error/5 active:scale-[0.98]"
        >
            <div
                class="flex h-8 w-8 items-center justify-center rounded-lg bg-error/10 transition-colors group-hover:bg-error/15"
            >
                <mat-icon class="text-[18px] text-error"> logout </mat-icon>
            </div>
            <span>Sign Out</span>
        </button>
    `,
    styles: ``,
})
export class SignOutButton {
    readonly onSignOut = output<void>();
}
