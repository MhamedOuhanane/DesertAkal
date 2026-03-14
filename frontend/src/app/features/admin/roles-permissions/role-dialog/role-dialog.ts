import { Component, inject, input, output, signal, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { MatIcon } from '@angular/material/icon';
import { TextInput } from '../../../../shared/components/text-input/text-input';
import { Permission } from '../../../../core/models/permission.model';
import { RoleFind } from '../../../../core/models/role.model';
import { PermissionService } from '../../../../core/services/permission-service';

@Component({
    selector: 'app-role-dialog',
    imports: [ReactiveFormsModule, MatIcon, TextInput],
    templateUrl: './role-dialog.html',
})
export class RoleDialog implements OnInit {
    private fb = inject(FormBuilder);
    private permissionService = inject(PermissionService);

    role = input<RoleFind | null>(null);
    isSubmitting = input(false);

    save = output<{ name: string; permissionUuids: string[] }>();
    cancel = output<void>();

    form!: FormGroup;
    allPermissions = signal<Permission[]>([]);
    selectedPermissions = signal<Permission[]>([]);
    loadingPerms = signal(true);
    permSearch = signal('');

    filteredPermissions = signal<Permission[]>([]);

    ngOnInit(): void {
        this.form = this.fb.group({
            name: [
                this.role()?.name || '',
                [
                    Validators.required,
                    Validators.minLength(4),
                    Validators.maxLength(50),
                    Validators.pattern(/^[A-Z_]+$/),
                ],
            ],
        });

        if (this.role()?.permissions) {
            this.selectedPermissions.set([...this.role()!.permissions]);
        }

        this.loadPermissions();
    }

    private async loadPermissions(): Promise<void> {
        this.loadingPerms.set(true);
        try {
            const res = await firstValueFrom(
                this.permissionService.findAll({
                    page: 0,
                    size: 200,
                    sortBy: 'name',
                    order: 'asc',
                }),
            );
            const perms = (res.data?.content as Permission[]) || [];
            this.allPermissions.set(perms);
            this.filteredPermissions.set(perms);
        } catch {
        } finally {
            this.loadingPerms.set(false);
        }

        setInterval(() => {
            const q = this.permSearch().toLowerCase();
            if (!q) {
                this.filteredPermissions.set(this.allPermissions());
            } else {
                this.filteredPermissions.set(
                    this.allPermissions().filter((p) => p.name.toLowerCase().includes(q)),
                );
            }
        }, 300);
    }

    togglePermission(perm: Permission): void {
        this.selectedPermissions.update((current) => {
            const exists = current.find((p) => p.uuid === perm.uuid);
            if (exists) {
                return current.filter((p) => p.uuid !== perm.uuid);
            }
            return [...current, perm];
        });
    }

    isSelected(uuid: string): boolean {
        return this.selectedPermissions().some((p) => p.uuid === uuid);
    }

    selectAll(): void {
        this.selectedPermissions.set([...this.allPermissions()]);
    }

    deselectAll(): void {
        this.selectedPermissions.set([]);
    }

    onSubmit(): void {
        if (this.form.invalid || this.selectedPermissions().length === 0) return;

        this.save.emit({
            name: this.form.value.name,
            permissionUuids: this.selectedPermissions().map((p) => p.uuid),
        });
    }
}
