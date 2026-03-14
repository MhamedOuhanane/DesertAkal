import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { CityFind } from '../../../../core/models/city.model';
import { Image } from '../../../../core/models/image.model';
import { TextInput } from '../../../../shared/components/text-input/text-input';
import { CityService } from '../../../../core/services/city-service';

@Component({
    selector: 'app-city-form',
    standalone: true,
    imports: [ReactiveFormsModule, RouterLink, MatIcon, TextInput],
    templateUrl: './city-form.html',
})
export class CityForm implements OnInit {
    private fb = inject(FormBuilder);
    private router = inject(Router);
    private route = inject(ActivatedRoute);
    private cityService = inject(CityService);

    form!: FormGroup;
    isEditMode = signal(false);
    isLoading = signal(true);
    isSubmitting = signal(false);
    cityUuid = signal<string | null>(null);
    existingCity = signal<CityFind | null>(null);

    newImageFiles = signal<File[]>([]);
    newImagePreviews = signal<string[]>([]);
    imagesToDelete = signal<string[]>([]);
    isUploadingImages = signal(false);
    isDeletingImages = signal(false);
    isSettingCover = signal(false);

    async ngOnInit(): Promise<void> {
        const uuid = this.route.snapshot.paramMap.get('uuid');

        if (uuid) {
            this.isEditMode.set(true);
            this.cityUuid.set(uuid);
        }

        this.buildForm();

        if (this.isEditMode()) {
            await this.loadCity(uuid!);
        }

        this.isLoading.set(false);
    }

    private buildForm(): void {
        this.form = this.fb.group({
            name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
            description: ['', [Validators.required, Validators.maxLength(5000)]],
            map_lat: [null, [Validators.required]],
            map_lng: [null, [Validators.required]],
        });
    }

    private async loadCity(uuid: string): Promise<void> {
        try {
            const res = await firstValueFrom(this.cityService.find(uuid));
            const city = res.data!;
            this.existingCity.set(city);

            this.form.patchValue({
                name: city.name,
                description: city.description,
                map_lat: city.map_lat,
                map_lng: city.map_lng,
            });
        } catch (err: any) {
            toast.error(err?.error?.message || 'City not found');
            this.router.navigate(['/dashboard/cities']);
        }
    }

    onFilesSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (!input.files) return;

        const files = Array.from(input.files);
        const validFiles = files.filter((f) => {
            if (!f.type.startsWith('image/')) {
                toast.error(`"${f.name}" is not an image`);
                return false;
            }
            if (f.size > 5 * 1024 * 1024) {
                toast.error(`"${f.name}" exceeds 5MB`);
                return false;
            }
            return true;
        });

        this.newImageFiles.update((prev) => [...prev, ...validFiles]);

        validFiles.forEach((file) => {
            const reader = new FileReader();
            reader.onload = () => {
                this.newImagePreviews.update((prev) => [...prev, reader.result as string]);
            };
            reader.readAsDataURL(file);
        });

        input.value = '';
    }

    removeNewImage(index: number): void {
        this.newImageFiles.update((files) => files.filter((_, i) => i !== index));
        this.newImagePreviews.update((previews) => previews.filter((_, i) => i !== index));
    }

    toggleImageToDelete(imageUuid: string): void {
        this.imagesToDelete.update((current) => {
            if (current.includes(imageUuid)) {
                return current.filter((id) => id !== imageUuid);
            }
            return [...current, imageUuid];
        });
    }

    isMarkedForDeletion(imageUuid: string): boolean {
        return this.imagesToDelete().includes(imageUuid);
    }

    async uploadImages(): Promise<void> {
        if (this.newImageFiles().length === 0 || !this.cityUuid()) return;

        this.isUploadingImages.set(true);
        try {
            const res = await firstValueFrom(
                this.cityService.addImages(this.cityUuid()!, this.newImageFiles()),
            );
            this.existingCity.set(res.data!);
            this.newImageFiles.set([]);
            this.newImagePreviews.set([]);
            toast.success('Images uploaded successfully');
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to upload images');
        } finally {
            this.isUploadingImages.set(false);
        }
    }

    async deleteSelectedImages(): Promise<void> {
        if (this.imagesToDelete().length === 0 || !this.cityUuid()) return;

        this.isDeletingImages.set(true);
        try {
            await firstValueFrom(
                this.cityService.deleteImages(this.cityUuid()!, this.imagesToDelete()),
            );
            const res = await firstValueFrom(this.cityService.find(this.cityUuid()!));
            this.existingCity.set(res.data!);
            this.imagesToDelete.set([]);
            toast.success('Images deleted successfully');
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to delete images');
        } finally {
            this.isDeletingImages.set(false);
        }
    }

    async setCover(imageUuid: string): Promise<void> {
        if (!this.cityUuid()) return;

        this.isSettingCover.set(true);
        try {
            await firstValueFrom(this.cityService.setCoverImage(this.cityUuid()!, imageUuid));
            const res = await firstValueFrom(this.cityService.find(this.cityUuid()!));
            this.existingCity.set(res.data!);
            toast.success('Cover image updated');
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to set cover');
        } finally {
            this.isSettingCover.set(false);
        }
    }

    async onSubmit(): Promise<void> {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.isSubmitting.set(true);

        try {
            if (this.isEditMode()) {
                await this.updateCity();
            } else {
                await this.createCity();
            }
        } catch (err: any) {
            toast.error(err?.error?.message || 'Operation failed');
        } finally {
            this.isSubmitting.set(false);
        }
    }

    private async createCity(): Promise<void> {
        const dto = this.form.value;
        const res = await firstValueFrom(this.cityService.create(dto));
        const createdUuid = res.data!.uuid;

        if (this.newImageFiles().length > 0) {
            try {
                await firstValueFrom(this.cityService.addImages(createdUuid, this.newImageFiles()));
            } catch {
                toast.warning('City created but some images failed to upload');
            }
        }

        toast.success(res.message || 'City created successfully');
        this.router.navigate(['/dashboard/cities', createdUuid]);
    }

    private async updateCity(): Promise<void> {
        const formValue = this.form.value;
        const city = this.existingCity()!;
        const dto: any = {};

        if (formValue.name && formValue.name !== city.name) dto.name = formValue.name;
        if (formValue.description && formValue.description !== city.description)
            dto.description = formValue.description;
        if (formValue.map_lat !== null && formValue.map_lat !== city.map_lat)
            dto.map_lat = formValue.map_lat;
        if (formValue.map_lng !== null && formValue.map_lng !== city.map_lng)
            dto.map_lng = formValue.map_lng;

        if (Object.keys(dto).length === 0) {
            toast.info('No changes detected');
            return;
        }

        const res = await firstValueFrom(this.cityService.update(this.cityUuid()!, dto));
        toast.success(res.message || 'City updated successfully');
        this.router.navigate(['/dashboard/cities', this.cityUuid()]);
    }

    get descriptionLength(): number {
        return this.form.get('description')?.value?.length || 0;
    }

    get existingImages(): Image[] {
        return this.existingCity()?.images || [];
    }

    get remainingImages(): Image[] {
        return this.existingImages.filter((img) => !this.imagesToDelete().includes(img.uuid));
    }
}
