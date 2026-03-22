import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { ReservationService } from '../../../../core/services/reservation-service';
import { TourService } from '../../../../core/services/tour-service';
import { GuideService } from '../../../../core/services/guide-service';
import { TourFind } from '../../../../core/models/tour.model';
import { Guide } from '../../../../core/models/guide.model';
import { StepTripDetails } from './components/step-trip-details/step-trip-details';
import { StepSelectGuide } from './components/step-select-guide/step-select-guide';
import { StepConfirm } from './components/step-confirm/step-confirm';
import { BookingSidebar } from './components/reservation-sidebar/reservation-sidebar';

@Component({
    selector: 'app-reservation-form',
    imports: [
        RouterLink,
        ReactiveFormsModule,
        MatIcon,
        StepTripDetails,
        StepSelectGuide,
        StepConfirm,
        BookingSidebar,
    ],
    templateUrl: './reservation-form.html',
})
export class ReservationForm implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private fb = inject(FormBuilder);
    private reservationService = inject(ReservationService);
    private tourService = inject(TourService);
    private guideService = inject(GuideService);
    readonly PRICE_PER_PERSON = 70;
    readonly CURRENCY = 'EUR';

    form!: FormGroup;
    tour = signal<TourFind | null>(null);
    guides = signal<Guide[]>([]);
    isLoading = signal(true);
    isSubmitting = signal(false);
    guidesLoading = signal(false);
    currentStep = signal(1);

    price = computed(() => this.PRICE_PER_PERSON * (this.tour()?.durationDays || 1));

    readonly steps = [
        { n: 1, label: 'Trip Details', icon: 'event' },
        { n: 2, label: 'Select Guide', icon: 'person' },
        { n: 3, label: 'Confirm', icon: 'check_circle' },
    ];

    minDate: string;

    constructor() {
        const d = new Date();
        d.setDate(d.getDate() + 7);
        this.minDate = d.toISOString().split('T')[0];
    }

    async ngOnInit(): Promise<void> {
        this.initForm();

        const tourUuid = this.route.snapshot.queryParamMap.get('tourUuid');
        if (!tourUuid) {
            toast.error('No tour selected');
            this.router.navigate(['/tours']);
            return;
        }

        await this.loadTour(tourUuid);
        this.isLoading.set(false);
    }

    private initForm(): void {
        this.form = this.fb.group({
            tourUuid: ['', Validators.required],
            guideUuid: ['', Validators.required],
            startDate: ['', Validators.required],
            numberPeople: [1, [Validators.required, Validators.min(1), Validators.max(20)]],
            amount: [0, [Validators.required, Validators.min(1)]],
        });
    }

    private async loadTour(uuid: string): Promise<void> {
        try {
            const res = await firstValueFrom(this.tourService.findOne(uuid));
            this.tour.set(res.data as TourFind);
            this.form.patchValue({ tourUuid: uuid });
            this.recalculateAmount();
        } catch {
            toast.error('Tour not found');
            this.router.navigate(['/tours']);
        }
    }

    async loadAvailableGuides(startDate: string, endDate: string): Promise<void> {
        if (!startDate || !endDate) return;

        this.guidesLoading.set(true);
        this.form.patchValue({ guideUuid: '' });

        try {
            const res = await firstValueFrom(
                this.guideService.getAvailableGuides({ startDate, endDate }),
            );
            this.guides.set(res.data || []);
        } catch {
            toast.error('Failed to load available guides');
            this.guides.set([]);
        } finally {
            this.guidesLoading.set(false);
        }
    }

    recalculateAmount(): void {
        const people = this.form.get('numberPeople')?.value || 1;
        this.form.patchValue({ amount: this.price() * people }, { emitEvent: false });
    }

    goToStep(step: number): void {
        this.currentStep.set(step);
    }

    nextStep(): void {
        this.currentStep.update((s) => Math.min(s + 1, 3));
    }

    prevStep(): void {
        this.currentStep.update((s) => Math.max(s - 1, 1));
    }

    async onSubmit(): Promise<void> {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.isSubmitting.set(true);
        try {
            const val = this.form.value;
            const res = await firstValueFrom(
                this.reservationService.create({
                    tourUuid: val.tourUuid,
                    guideUuid: val.guideUuid,
                    startDate: new Date(val.startDate).toISOString(),
                    numberPeople: val.numberPeople,
                    amount: val.amount,
                }),
            );
            toast.success('Reservation created! Proceed to payment.');
            this.router.navigate(['/tourist/dashboard/bookings', res.data!.uuid, 'pay']);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to create reservation');
        } finally {
            this.isSubmitting.set(false);
        }
    }

    getEndDate(): string {
        const start = this.form.get('startDate')?.value;
        const dur = this.tour()?.durationDays || 0;
        if (!start || !dur) return '';
        const d = new Date(start);
        d.setDate(d.getDate() + dur);
        return d.toISOString().split('T')[0];
    }

    getSelectedGuide(): Guide | null {
        const guideUuid = this.form.get('guideUuid')?.value;
        return this.guides().find((g) => g.uuid === guideUuid) || null;
    }
}
