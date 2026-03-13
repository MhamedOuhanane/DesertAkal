import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { Language } from '../../../../core/models/language.model';
import { GuideFind, GuideUpdate } from '../../../../core/models/guide.model';
import { GuideService } from '../../../../core/services/guide-service';
import { LanguageService } from '../../../../core/services/language-service';
import { MatIcon } from '@angular/material/icon';
import { TextInput } from '../../../../shared/components/text-input/text-input';

@Component({
    selector: 'app-guide-form',
    imports: [ReactiveFormsModule, RouterLink, MatIcon, TextInput],
    templateUrl: './guide-form.html',
})
export class GuideForm implements OnInit {
    private fb = inject(FormBuilder);
    private router = inject(Router);
    private route = inject(ActivatedRoute);
    private guideService = inject(GuideService);
    private languageService = inject(LanguageService);

    form!: FormGroup;
    isEditMode = signal(false);
    isLoading = signal(true);
    isSubmitting = signal(false);
    showPassword = signal(false);
    showConfirmPassword = signal(false);
    guideUuid = signal<string | null>(null);
    existingGuide = signal<GuideFind | null>(null);

    availableLanguages = signal<Language[]>([]);
    selectedLanguages = signal<Language[]>([]);

    async ngOnInit(): Promise<void> {
        const uuid = this.route.snapshot.paramMap.get('uuid');

        if (uuid) {
            this.isEditMode.set(true);
            this.guideUuid.set(uuid);
        }

        this.buildForm();
        await this.loadLanguages();

        if (this.isEditMode()) {
            await this.loadGuide(uuid!);
        }

        this.isLoading.set(false);
    }

    private buildForm(): void {
        if (this.isEditMode()) {
            // Edit mode - only updatable fields
            this.form = this.fb.group({
                firstName: ['', [Validators.minLength(3), Validators.maxLength(50)]],
                lastName: ['', [Validators.minLength(3), Validators.maxLength(50)]],
                phone: ['', [Validators.pattern(/^(\+\d{1,3}[- ]?)?\d{6,15}$/)]],
                experienceYears: [null, [Validators.min(0), Validators.max(50)]],
            });
        } else {
            // Create mode - all fields
            this.form = this.fb.group(
                {
                    firstName: [
                        '',
                        [Validators.required, Validators.minLength(3), Validators.maxLength(50)],
                    ],
                    lastName: [
                        '',
                        [Validators.required, Validators.minLength(3), Validators.maxLength(50)],
                    ],
                    username: [
                        '',
                        [
                            Validators.required,
                            Validators.minLength(7),
                            Validators.maxLength(20),
                            Validators.pattern(/^[a-zA-Z0-9._-]+$/),
                        ],
                    ],
                    email: ['', [Validators.required, Validators.email]],
                    password: [
                        '',
                        [
                            Validators.required,
                            Validators.minLength(8),
                            Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/),
                        ],
                    ],
                    confirmPassword: ['', [Validators.required, Validators.minLength(8)]],
                    phone: [
                        '',
                        [Validators.required, Validators.pattern(/^\+\d{1,3}[- ]?\d{6,15}$/)],
                    ],
                    experienceYears: [
                        0,
                        [Validators.required, Validators.min(0), Validators.max(50)],
                    ],
                },
                { validators: this.passwordMatchValidator },
            );
        }
    }

    private passwordMatchValidator(group: FormGroup): { [key: string]: boolean } | null {
        const password = group.get('password')?.value;
        const confirm = group.get('confirmPassword')?.value;
        if (password && confirm && password !== confirm) {
            return { passwordMismatch: true };
        }
        return null;
    }

    private async loadLanguages(): Promise<void> {
        try {
            const res = await firstValueFrom(this.languageService.findAll({ size: 200 }));
            this.availableLanguages.set(res.data?.content || []);
        } catch (err: any) {
            const message = err?.error?.message || 'Failed to load languages';
            toast.error(message);
        }
    }

    private async loadGuide(uuid: string): Promise<void> {
        try {
            const res = await firstValueFrom(this.guideService.findOne(uuid));
            const guide = res.data;
            this.existingGuide.set(guide);

            this.form.patchValue({
                firstName: guide?.firstName,
                lastName: guide?.lastName,
                phone: guide?.phone,
                experienceYears: guide?.experienceYears,
            });

            this.selectedLanguages.set([...(guide?.languages || [])]);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Guide not found');
            this.router.navigate(['/dashboard/guides']);
        }
    }

    toggleLanguage(language: Language): void {
        this.selectedLanguages.update((current) => {
            const exists = current.find((l) => l.uuid === language.uuid);
            if (exists) {
                return current.filter((l) => l.uuid !== language.uuid);
            }
            return [...current, language];
        });
    }

    isLanguageSelected(uuid: string): boolean {
        return this.selectedLanguages().some((l) => l.uuid === uuid);
    }

    async onSubmit(): Promise<void> {
        if (this.form.invalid) {
            Object.keys(this.form.controls).forEach((key) => {
                this.form.get(key)?.markAsTouched();
            });
            return;
        }

        if (!this.isEditMode() && this.selectedLanguages().length === 0) {
            toast.error('Please select at least one language');
            return;
        }

        this.isSubmitting.set(true);

        try {
            if (this.isEditMode()) {
                await this.updateGuide();
            } else {
                await this.createGuide();
            }
        } catch (err: any) {
            toast.error(err?.error?.message || 'Operation failed');
        } finally {
            this.isSubmitting.set(false);
        }
    }

    private async createGuide(): Promise<void> {
        const formValue = this.form.value;

        const dto = {
            ...formValue,
            languageUsUuids: this.selectedLanguages().map((l) => l.uuid),
        };

        const res = await firstValueFrom(this.guideService.create(dto));
        toast.success(res.message || 'Guide created successfully');
        this.router.navigate(['/dashboard/guides', res.data?.uuid]);
    }

    private async updateGuide(): Promise<void> {
        const formValue = this.form.value;

        const dto: GuideUpdate = {};
        const guide = this.existingGuide()!;

        if (formValue.firstName && formValue.firstName !== guide.firstName)
            dto.firstName = formValue.firstName;
        if (formValue.lastName && formValue.lastName !== guide.lastName)
            dto.lastName = formValue.lastName;
        if (formValue.phone && formValue.phone !== guide.phone) dto.phone = formValue.phone;
        if (
            formValue.experienceYears !== null &&
            formValue.experienceYears !== guide.experienceYears
        )
            dto.experienceYears = formValue.experienceYears;

        const currentLangUuids = guide.languages
            .map((l) => l.uuid)
            .sort()
            .join(',');
        const newLangUuids = this.selectedLanguages()
            .map((l) => l.uuid)
            .sort()
            .join(',');

        if (currentLangUuids !== newLangUuids) {
            dto.languageUsUuids = this.selectedLanguages().map((l) => l.uuid);
        }

        if (Object.keys(dto).length === 0) {
            toast.info('No changes detected');
            return;
        }

        const res = await firstValueFrom(this.guideService.update(this.guideUuid()!, dto));
        toast.success(res.message || 'Guide updated successfully');
        this.router.navigate(['/dashboard/guides', this.guideUuid()]);
    }

    hasError(field: string): boolean {
        const control = this.form.get(field);
        return !!(control && control.invalid && control.touched);
    }

    getError(field: string): string {
        const control = this.form.get(field);
        if (!control || !control.errors) return '';
        if (control.errors['required']) return `${field} is required`;
        if (control.errors['minlength'])
            return `Minimum ${control.errors['minlength'].requiredLength} characters`;
        if (control.errors['maxlength'])
            return `Maximum ${control.errors['maxlength'].requiredLength} characters`;
        if (control.errors['email']) return 'Invalid email';
        if (control.errors['pattern']) return 'Invalid format';
        if (control.errors['min']) return `Minimum value is ${control.errors['min'].min}`;
        if (control.errors['max']) return `Maximum value is ${control.errors['max'].max}`;
        return 'Invalid';
    }

    languagesDisplay = computed(() => {
        return (
            this.selectedLanguages()
                .map((l) => l.name)
                .join(', ') || 'None'
        );
    });
}
