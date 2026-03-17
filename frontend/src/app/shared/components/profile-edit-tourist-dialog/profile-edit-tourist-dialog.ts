import { Component, inject, input, output, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { TextInput } from '../text-input/text-input';
import { ProfileService } from '../../../core/services/profile-service';
import { Tourist, TouristUpdate } from '../../../core/models/tourist.model';

@Component({
    selector: 'app-profile-edit-tourist-dialog',
    imports: [ReactiveFormsModule, MatIcon, TextInput],
    templateUrl: './profile-edit-tourist-dialog.html',
})
export class ProfileEditTouristDialog implements OnInit {
    private fb = inject(FormBuilder);
    private profileService = inject(ProfileService);

    profile = input.required<Tourist>();
    saved = output<void>();
    cancel = output<void>();

    form!: FormGroup;
    isSubmitting = signal(false);

    ngOnInit(): void {
        const p = this.profile();
        this.form = this.fb.group({
            firstName: [p.firstName, [Validators.minLength(3), Validators.maxLength(50)]],
            lastName: [p.lastName, [Validators.minLength(3), Validators.maxLength(50)]],
            phone: [p.phone, [Validators.pattern(/^(\+\d{1,3}[- ]?)?\d{6,15}$/)]],
            nationality: [p.nationality, [Validators.maxLength(50)]],
            language: [p.language, [Validators.maxLength(20)]],
        });
    }

    async onSubmit(): Promise<void> {
        if (this.form.invalid) return;
        this.isSubmitting.set(true);
        try {
            const dto: TouristUpdate = {};
            const p = this.profile();
            const v = this.form.value;
            if (v.firstName !== p.firstName) dto.firstName = v.firstName;
            if (v.lastName !== p.lastName) dto.lastName = v.lastName;
            if (v.phone !== p.phone) dto.phone = v.phone;
            if (v.nationality !== p.nationality) dto.nationality = v.nationality;
            if (v.language !== p.language) dto.language = v.language;

            if (Object.keys(dto).length === 0) {
                toast.info('No changes');
                this.cancel.emit();
                return;
            }

            await firstValueFrom(this.profileService.updateTourist(p.uuid, dto));
            this.saved.emit();
        } catch (err: any) {
            toast.error(err?.error?.message || 'Update failed');
        } finally {
            this.isSubmitting.set(false);
        }
    }
}
