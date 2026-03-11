import { Component, input, output } from '@angular/core';

@Component({
    selector: 'app-delete-dialog',
    template: `
        <div
            class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4 backdrop-blur-sm"
            (click)="cancel.emit()"
        >
            <div
                class="animate-scale-in w-full max-w-sm rounded-2xl border border-border bg-surface p-6 shadow-2xl"
                (click)="$event.stopPropagation()"
            >
                <div class="mb-4 flex justify-center">
                    <div class="flex h-14 w-14 items-center justify-center rounded-2xl bg-error/10">
                        <svg
                            class="h-7 w-7 text-error"
                            fill="none"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                            stroke-width="1.5"
                        >
                            <path
                                stroke-linecap="round"
                                stroke-linejoin="round"
                                d="M14.74 9l-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 01-2.244 2.077H8.084a2.25 2.25 0 01-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 00-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 013.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 00-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 00-7.5 0"
                            />
                        </svg>
                    </div>
                </div>
                <h3 class="text-center text-lg font-bold text-text-primary">
                    Delete {{ itemName() }}?
                </h3>
                <p class="mt-2 text-center text-sm text-text-secondary">
                    Are you sure you want to delete <strong>"{{ itemTitle() }}"</strong>? This
                    action cannot be undone.
                </p>
                <div class="mt-6 flex gap-3">
                    <button
                        (click)="cancel.emit()"
                        class="btn-secondary flex-1 justify-center py-2.5 text-sm"
                    >
                        Cancel
                    </button>
                    <button
                        (click)="confirm.emit()"
                        class="flex flex-1 items-center justify-center gap-2 rounded-xl bg-error px-4 py-2.5 text-sm font-bold text-white transition-all duration-200 hover:bg-error/90 active:scale-95"
                    >
                        Delete
                    </button>
                </div>
            </div>
        </div>
    `,
})
export class DeleteDialog {
    itemName = input('item');
    itemTitle = input.required<string>();
    confirm = output<void>();
    cancel = output<void>();
}
