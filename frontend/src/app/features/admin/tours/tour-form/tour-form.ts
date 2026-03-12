import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { City } from '../../../../core/models/city.model';
import { TourCreate, TourFind, TourUpdate } from '../../../../core/models/tour.model';
import { toast } from 'ngx-sonner';
import { firstValueFrom } from 'rxjs';
import { TourService } from '../../../../core/services/tour-service';
import { CityService } from '../../../../core/services/city-service';
import { CityTourCreate } from '../../../../core/models/city-tour.model';

@Component({
    selector: 'app-tour-form',
    standalone: true,
    imports: [ReactiveFormsModule, RouterLink],
    templateUrl: './tour-form.html',
})
export class TourForm implements OnInit {
    private fb = inject(FormBuilder);
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private tourService = inject(TourService);
    private cityService = inject(CityService);

    isEditMode = signal(false);
    isLoading = signal(false);
    isLoadingData = signal(true);
    isSaving = signal(false);

    tourUuid = signal<string | null>(null);
    existingTour = signal<TourFind | null>(null);
    cities = signal<City[]>([]);
    imagePreview = signal<string | null>(null);
    selectedFile = signal<File | null>(null);

    tourForm = this.fb.nonNullable.group({
        title: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(100)]],
        description: ['', [Validators.required, Validators.maxLength(5000)]],
        cityTours: this.fb.array<FormGroup>([], [Validators.required]),
    });

    get cityToursArray(): FormArray {
        return this.tourForm.get('cityTours') as FormArray;
    }

    async ngOnInit(): Promise<void> {
        const uuid = this.route.snapshot.paramMap.get('uuid');
        if (uuid) {
            this.isEditMode.set(true);
            this.tourUuid.set(uuid);
        }

        await this.loadCities();

        if (this.isEditMode()) {
            await this.loadTour();
        } else {
            this.addCityTour();
            this.isLoadingData.set(false);
        }
    }

    private async loadCities(): Promise<void> {
        try {
            const res = await firstValueFrom(this.cityService.findAll({}));
            this.cities.set(res.data?.content ?? []);
        } catch {
            toast.error('Failed to load cities.');
        }
    }

    private async loadTour(): Promise<void> {
        try {
            const res = await firstValueFrom(this.tourService.findOne(this.tourUuid()!));
            const tour = res.data;
            if (!tour) return;

            this.existingTour.set(tour);
            this.imagePreview.set(tour.image);

            this.tourForm.patchValue({
                title: tour.title,
                description: tour.description,
            });

            if (tour.cityTours && Array.isArray(tour.cityTours)) {
                tour.cityTours.forEach((ct: any) => {
                    this.cityToursArray.push(
                        this.fb.group({
                            orderIndex: [
                                ct.orderIndex ?? ct.city?.orderIndex,
                                [Validators.required, Validators.min(1)],
                            ],
                            daysCount: [
                                ct.daysCount ?? ct.city?.daysCount,
                                [Validators.required, Validators.min(1)],
                            ],
                            cityUuid: [ct.city?.uuid ?? ct.cityUuid, [Validators.required]],
                        }),
                    );
                });
            }
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to load tour.');
            this.router.navigate(['/dashboard/tours']);
        } finally {
            this.isLoadingData.set(false);
        }
    }

    addCityTour(): void {
        const cityGroup = this.fb.group({
            orderIndex: [this.cityToursArray.length + 1, [Validators.required, Validators.min(1)]],
            daysCount: [1, [Validators.required, Validators.min(1)]],
            cityUuid: ['', [Validators.required]],
        });
        this.cityToursArray.push(cityGroup);
    }

    removeCityTour(index: number): void {
        this.cityToursArray.removeAt(index);
        this.cityToursArray.controls.forEach((ctrl, i) => {
            ctrl.get('orderIndex')?.setValue(i + 1);
        });
    }

    onImageSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        const file = input.files?.[0];
        if (!file) return;

        if (!file.type.startsWith('image/')) {
            toast.error('Please select an image file.');
            return;
        }
        if (file.size > 5 * 1024 * 1024) {
            toast.error('Image must be under 5MB.');
            return;
        }

        this.selectedFile.set(file);

        const reader = new FileReader();
        reader.onload = () => this.imagePreview.set(reader.result as string);
        reader.readAsDataURL(file);
    }

    get totalDays(): number {
        return this.cityToursArray.controls.reduce(
            (sum, ctrl) => sum + (ctrl.get('daysCount')?.value || 0),
            0,
        );
    }

    async onSubmit(): Promise<void> {
        if (this.tourForm.invalid) {
            this.tourForm.markAllAsTouched();
            toast.error('Please fix the form errors.');
            return;
        }

        if (!this.isEditMode() && !this.selectedFile()) {
            toast.error('Please select a cover image.');
            return;
        }

        this.isSaving.set(true);

        try {
            const formValue = this.tourForm.getRawValue();

            if (this.isEditMode()) {
                const updateData: TourUpdate = {
                    title: formValue.title,
                    description: formValue.description,
                    cityTours: formValue.cityTours as unknown as CityTourCreate[],
                };
                await firstValueFrom(this.tourService.update(this.tourUuid()!, updateData));

                if (this.selectedFile()) {
                    await firstValueFrom(
                        this.tourService.updateImage(this.tourUuid()!, this.selectedFile()!),
                    );
                }

                toast.success('Tour updated successfully!');
            } else {
                const createData: TourCreate = {
                    title: formValue.title,
                    description: formValue.description,
                    cityTours: formValue.cityTours as unknown as CityTourCreate[],
                };
                await firstValueFrom(this.tourService.create(createData, this.selectedFile()!));
                toast.success('Tour created successfully!');
            }

            this.router.navigate(['/dashboard/tours']);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to save tour.');
        } finally {
            this.isSaving.set(false);
        }
    }
}
