import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { AuthStore } from '../../../core/auth/auth.store';
import { RoleEnum } from '../../../core/enums/role.enum';
import { ProfileData } from '../../../core/models/user.models';
import { Tourist } from '../../../core/models/tourist.model';
import { GuideFind } from '../../../core/models/guide.model';
import { ProfileService } from '../../../core/services/profile-service';

@Component({
    selector: 'app-profile',
    imports: [DatePipe, DecimalPipe, MatIcon],
    templateUrl: './profile.html',
})
export class Profile implements OnInit {
    private authStore = inject(AuthStore);
    private profileService = inject(ProfileService);

    profile = signal<ProfileData | null>(null);
    isLoading = signal(true);
    isUploadingPhoto = signal(false);

    showEditDialog = signal(false);
    isSubmitting = signal(false);

    role = computed(() => this.authStore.userRole());
    userUuid = computed(() => this.authStore.user()?.uuid || '');

    isTourist = computed(() => this.role() === RoleEnum.TOURIST);
    isGuide = computed(() => this.role() === RoleEnum.GUIDE);
    isAdmin = computed(() => this.role() === RoleEnum.ADMIN);

    asTourist = computed(() => this.profile() as Tourist);
    asGuide = computed(() => this.profile() as GuideFind);

    async ngOnInit(): Promise<void> {
        await this.loadProfile();
    }

    async loadProfile(): Promise<void> {
        this.isLoading.set(true);
        try {
            const uuid = this.userUuid();
            let data: ProfileData;

            switch (this.role()) {
                case RoleEnum.TOURIST: {
                    const res = await firstValueFrom(this.profileService.getTourist(uuid));
                    data = res.data!;
                    break;
                }
                case RoleEnum.GUIDE: {
                    const res = await firstValueFrom(this.profileService.getGuide(uuid));
                    data = res.data!;
                    break;
                }
                default: {
                    const res = await firstValueFrom(this.profileService.getUser(uuid));
                    data = res.data!;
                    break;
                }
            }

            this.profile.set(data);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to load profile');
        } finally {
            this.isLoading.set(false);
        }
    }

    async onPhotoSelected(event: Event): Promise<void> {
        const input = event.target as HTMLInputElement;
        if (!input.files?.[0]) return;

        const file = input.files[0];

        if (!file.type.startsWith('image/')) {
            toast.error('Please select an image file');
            return;
        }
        if (file.size > 5 * 1024 * 1024) {
            toast.error('Image must be less than 5MB');
            return;
        }

        this.isUploadingPhoto.set(true);
        try {
            await firstValueFrom(this.profileService.updateUserPhoto(this.userUuid(), file));
            toast.success('Photo updated successfully');
            await this.loadProfile();
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to update photo');
        } finally {
            this.isUploadingPhoto.set(false);
            input.value = '';
        }
    }

    openEditDialog(): void {
        this.showEditDialog.set(true);
    }

    closeEditDialog(): void {
        this.showEditDialog.set(false);
    }

    async onProfileUpdated(): Promise<void> {
        this.showEditDialog.set(false);
        await this.loadProfile();
        toast.success('Profile updated successfully');
    }

    getInitials(): string {
        const p = this.profile();
        if (!p) return '?';
        return `${p.firstName?.charAt(0) || ''}${p.lastName?.charAt(0) || ''}`.toUpperCase();
    }

    getStatusConfig(status: string): { bg: string; text: string; dot: string } {
        const map: Record<string, any> = {
            ACTIVE: { bg: 'bg-green-500/10', text: 'text-green-600', dot: 'bg-green-500' },
            INACTIVE: { bg: 'bg-gray-500/10', text: 'text-gray-500', dot: 'bg-gray-400' },
            BANNED: { bg: 'bg-red-500/10', text: 'text-red-600', dot: 'bg-red-500' },
            SUSPENDED: { bg: 'bg-orange-500/10', text: 'text-orange-600', dot: 'bg-orange-500' },
        };
        return map[status] || { bg: 'bg-gray-500/10', text: 'text-gray-500', dot: 'bg-gray-400' };
    }

    getRoleBadge(): string {
        switch (this.role()) {
            case RoleEnum.ADMIN:
                return 'bg-purple-500/10 text-purple-600';
            case RoleEnum.GUIDE:
                return 'bg-blue-500/10 text-blue-600';
            case RoleEnum.TOURIST:
                return 'bg-teal-500/10 text-teal-600';
            default:
                return 'bg-gray-500/10 text-gray-500';
        }
    }

    protected readonly RoleEnum = RoleEnum;
}
