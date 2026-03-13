import { Component, input, output } from '@angular/core';

@Component({
    selector: 'app-delete-dialog',
    standalone: true,
    template: `
        <div
            class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4 backdrop-blur-sm"
            (click)="cancel.emit()"
        >
            <div
                class="w-full max-w-sm rounded-2xl border border-border bg-surface p-6 shadow-2xl"
                (click)="$event.stopPropagation()"
            >
                <h3 class="text-center text-lg font-bold text-text-primary">
                    Delete {{ itemName() }}?
                </h3>
                <p class="mt-2 text-center text-sm text-text-secondary">
                    Are you sure you want to delete <strong>"{{ itemTitle() }}"</strong>? This
                    action cannot be undone.
                </p>

                <div class="mt-6 flex gap-3">
                    <button
                        [disabled]="isDeleting()"
                        (click)="cancel.emit()"
                        class="btn-secondary flex-1 justify-center py-2.5 text-sm disabled:opacity-50"
                    >
                        Cancel
                    </button>
                    <button
                        [disabled]="isDeleting()"
                        (click)="confirm.emit()"
                        class="flex flex-1 items-center justify-center gap-2 rounded-xl bg-red-600 px-4 py-2.5 text-sm font-bold text-white transition-all hover:bg-red-700 active:scale-95 disabled:opacity-50"
                    >
                        @if (isDeleting()) {
                            <span
                                class="h-4 w-4 animate-spin border-2 border-white/30 border-t-white rounded-full"
                            ></span>
                            <span>Deleting...</span>
                        } @else {
                            <span>Delete</span>
                        }
                    </button>
                </div>
            </div>
        </div>
    `,
})
export class DeleteDialog {
    itemName = input('item');
    itemTitle = input.required<string>();
    isDeleting = input(false);
    confirm = output<void>();
    cancel = output<void>();
}
