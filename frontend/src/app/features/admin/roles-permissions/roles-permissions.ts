import { Component, computed, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { Role, RoleFind, RoleFilters } from '../../../core/models/role.model';
import { Permission, PermissionFilters } from '../../../core/models/permission.model';
import { Pagination } from '../../../core/models/response.models';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';
import { DeleteDialog } from '../../../shared/components/delete-dialog/delete-dialog';
import { PermissionDialog } from './permission-dialog/permission-dialog';
import { RoleDialog } from './role-dialog/role-dialog';
import { RoleService } from '../../../core/services/role-service';
import { PermissionService } from '../../../core/services/permission-service';

@Component({
    selector: 'app-roles-permissions',
    imports: [
        FormsModule,
        MatIcon,
        PaginationComponent,
        DeleteDialog,
        PermissionDialog,
        RoleDialog,
    ],
    templateUrl: './roles-permissions.html',
})
export class RolesPermissions {
    private roleService = inject(RoleService);
    private permissionService = inject(PermissionService);

    activeTab = signal<'roles' | 'permissions'>('roles');

    // ══════ ROLES ══════
    rolePagination = signal<Pagination<Role> | null>(null);
    roles = computed<Role[]>(() => this.rolePagination()?.content || []);
    rolesLoading = signal(true);
    roleQuery = signal<RoleFilters>({
        page: 0,
        size: 6,
        sortBy: 'name',
        order: 'asc',
        search: '',
    });

    showRoleDialog = signal(false);
    roleToEdit = signal<RoleFind | null>(null);
    isRoleSubmitting = signal(false);

    showRoleDeleteDialog = signal(false);
    roleToDelete = signal<Role | null>(null);
    isRoleDeleting = signal(false);

    // ══════ PERMISSIONS ══════
    permPagination = signal<Pagination<Permission> | null>(null);
    permissions = computed<Permission[]>(() => this.permPagination()?.content || []);
    permsLoading = signal(true);
    permQuery = signal<PermissionFilters>({
        page: 0,
        size: 6,
        sortBy: 'name',
        order: 'asc',
        search: '',
    });

    showPermDialog = signal(false);
    permToEdit = signal<Permission | null>(null);
    isPermSubmitting = signal(false);

    showPermDeleteDialog = signal(false);
    permToDelete = signal<Permission | null>(null);
    isPermDeleting = signal(false);

    private roleSearchSubject = new Subject<string>();
    private permSearchSubject = new Subject<string>();

    constructor() {
        this.roleSearchSubject
            .pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed())
            .subscribe((search) => {
                this.roleQuery.update((q) => ({ ...q, search, page: 0 }));
            });

        this.permSearchSubject
            .pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed())
            .subscribe((search) => {
                this.permQuery.update((q) => ({ ...q, search, page: 0 }));
            });

        effect(() => {
            this.loadRoles();
        });

        effect(() => {
            this.loadPermissions();
        });
    }

    // ══════════ ROLES LOGIC ══════════

    async loadRoles(): Promise<void> {
        this.rolesLoading.set(true);
        try {
            const res = await firstValueFrom(this.roleService.findAll(this.roleQuery()));
            if (res.data) this.rolePagination.set(res.data);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to load roles');
        } finally {
            this.rolesLoading.set(false);
        }
    }

    onRoleSearch(value: string): void {
        this.roleSearchSubject.next(value);
    }

    roleGoToPage(page: number): void {
        this.roleQuery.update((q) => ({ ...q, page }));
        this.loadRoles();
    }

    openCreateRole(): void {
        this.roleToEdit.set(null);
        this.showRoleDialog.set(true);
    }

    async openEditRole(role: Role): Promise<void> {
        try {
            const res = await firstValueFrom(this.roleService.findOne(role.uuid));
            this.roleToEdit.set(res.data!);
            this.showRoleDialog.set(true);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to load role details');
        }
    }

    closeRoleDialog(): void {
        this.showRoleDialog.set(false);
        this.roleToEdit.set(null);
    }

    async onRoleSave(data: { name: string; permissionUuids: string[] }): Promise<void> {
        this.isRoleSubmitting.set(true);
        try {
            const editing = this.roleToEdit();

            if (editing) {
                const dto: any = {};
                if (data.name !== editing.name) dto.name = data.name;

                const currentIds = editing.permissions
                    .map((p) => p.uuid)
                    .sort()
                    .join(',');
                const newIds = data.permissionUuids.sort().join(',');
                if (currentIds !== newIds) dto.permissionUuids = data.permissionUuids;

                if (Object.keys(dto).length === 0) {
                    toast.info('No changes detected');
                    this.closeRoleDialog();
                    return;
                }

                await firstValueFrom(this.roleService.update(editing.uuid, dto));
                toast.success('Role updated successfully');
            } else {
                await firstValueFrom(this.roleService.create(data));
                toast.success('Role created successfully');
            }

            this.closeRoleDialog();
            await this.loadRoles();
        } catch (err: any) {
            toast.error(err?.error?.message || 'Operation failed');
        } finally {
            this.isRoleSubmitting.set(false);
        }
    }

    openRoleDeleteDialog(role: Role, event: Event): void {
        event.stopPropagation();
        this.roleToDelete.set(role);
        this.showRoleDeleteDialog.set(true);
    }

    async confirmRoleDelete(): Promise<void> {
        const role = this.roleToDelete();
        if (!role) return;
        this.isRoleDeleting.set(true);
        try {
            await firstValueFrom(this.roleService.delete(role.uuid));
            toast.success(`Role "${role.name}" deleted`);
            this.showRoleDeleteDialog.set(false);
            this.roleToDelete.set(null);
            await this.loadRoles();
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to delete role');
        } finally {
            this.isRoleDeleting.set(false);
        }
    }

    // ══════════ PERMISSIONS LOGIC ══════════

    async loadPermissions(): Promise<void> {
        this.permsLoading.set(true);
        try {
            const res = await firstValueFrom(this.permissionService.findAll(this.permQuery()));
            if (res.data) this.permPagination.set(res.data);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to load permissions');
        } finally {
            this.permsLoading.set(false);
        }
    }

    onPermSearch(value: string): void {
        this.permSearchSubject.next(value);
    }

    permGoToPage(page: number): void {
        this.permQuery.update((q) => ({ ...q, page }));
        this.loadPermissions();
    }

    openCreatePerm(): void {
        this.permToEdit.set(null);
        this.showPermDialog.set(true);
    }

    openEditPerm(perm: Permission): void {
        this.permToEdit.set(perm);
        this.showPermDialog.set(true);
    }

    closePermDialog(): void {
        this.showPermDialog.set(false);
        this.permToEdit.set(null);
    }

    async onPermSave(data: { name: string }): Promise<void> {
        this.isPermSubmitting.set(true);
        try {
            const editing = this.permToEdit();
            if (editing) {
                if (data.name === editing.name) {
                    toast.info('No changes detected');
                    this.closePermDialog();
                    return;
                }
                await firstValueFrom(this.permissionService.update(editing.uuid, data));
                toast.success('Permission updated');
            } else {
                await firstValueFrom(this.permissionService.create(data));
                toast.success('Permission created');
            }
            this.closePermDialog();
            await this.loadPermissions();
        } catch (err: any) {
            toast.error(err?.error?.message || 'Operation failed');
        } finally {
            this.isPermSubmitting.set(false);
        }
    }

    openPermDeleteDialog(perm: Permission, event: Event): void {
        event.stopPropagation();
        this.permToDelete.set(perm);
        this.showPermDeleteDialog.set(true);
    }

    async confirmPermDelete(): Promise<void> {
        const perm = this.permToDelete();
        if (!perm) return;
        this.isPermDeleting.set(true);
        try {
            await firstValueFrom(this.permissionService.delete(perm.uuid));
            toast.success(`Permission "${perm.name}" deleted`);
            this.showPermDeleteDialog.set(false);
            this.permToDelete.set(null);
            await this.loadPermissions();
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to delete');
        } finally {
            this.isPermDeleting.set(false);
        }
    }
}
