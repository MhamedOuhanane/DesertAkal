import { Component, inject, input, output, signal, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatIcon } from '@angular/material/icon';
import { TextInput } from '../../../../shared/components/text-input/text-input';
import { Language } from '../../../../core/models/language.model';

@Component({
    selector: 'app-language-dialog',
    imports: [ReactiveFormsModule, MatIcon, TextInput],
    templateUrl: './language-dialog.html',
})
export class LanguageDialog implements OnInit {
    private fb = inject(FormBuilder);

    language = input<Language | null>(null);
    isSubmitting = input(false);

    save = output<{ name: string; code: string }>();
    cancel = output<void>();

    form!: FormGroup;

    ngOnInit(): void {
        this.form = this.fb.group({
            name: [
                this.language()?.name || '',
                [Validators.required, Validators.minLength(4), Validators.maxLength(50)],
            ],
            code: [
                this.language()?.code || '',
                [Validators.required, Validators.pattern(/^[a-z]{2,3}$/)],
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
