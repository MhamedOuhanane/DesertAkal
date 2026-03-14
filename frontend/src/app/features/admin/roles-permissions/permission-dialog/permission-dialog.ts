import { Component, inject, input, output, signal, OnInit } from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatIcon } from '@angular/material/icon';
import { TextInput } from '../../../../shared/components/text-input/text-input';
import { Permission } from '../../../../core/models/permission.model';

@Component({
    selector: 'app-permission-dialog',
    imports: [ReactiveFormsModule, MatIcon, TextInput],
    templateUrl: './permission-dialog.html',
})
export class PermissionDialog implements OnInit {
    private fb = inject(FormBuilder);

    permission = input<Permission | null>(null);
    isSubmitting = input(false);

    save = output<{ name: string }>();
    cancel = output<void>();

    form!: FormGroup;

    ngOnInit(): void {
        this.form = this.fb.group({
            name: [
                this.permission()?.name || '',
                [
                    Validators.required,
                    Validators.minLength(5),
                    Validators.maxLength(50),
                    Validators.pattern(/^[A-Z_]+$/),
                ],
            ],
        });
    }

    onSubmit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }
        this.save.emit(this.form.value);
    }
}