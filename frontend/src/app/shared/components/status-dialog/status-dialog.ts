import { Component, effect, input, output, signal } from '@angular/core';
import { User } from '../../../core/models/user.models';

@Component({
    selector: 'app-status-dialog',
    imports: [],
    templateUrl: './status-dialog.html',
    styles: ``,
})
export class StatusDialog {
    user = input.required<User>();
    isUpdating = input<boolean>(false);

    confirm = output<string>();
    cancel = output<void>();

    selectedStatus = signal<string>('');

    readonly statuses = ['ACTIVE', 'BANNED', 'SUSPENDED'];

    constructor() {
        effect(() => {
            this.selectedStatus.set(this.user().status);
        });
    }

    getStatusConfig(status: string) {
        const configs: any = {
            ACTIVE: { dot: 'bg-green-500', text: 'text-green-600' },
            //   INACTIVE: { dot: 'bg-gray-400', text: 'text-gray-500' },
            BANNED: { dot: 'bg-red-500', text: 'text-red-600' },
            SUSPENDED: { dot: 'bg-orange-500', text: 'text-orange-600' },
        };
        return configs[status] || configs['ACTIVE'];
    }
}
