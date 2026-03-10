import { CommonModule } from '@angular/common';
import { Component, input, Optional, Self, signal } from '@angular/core';
import { ControlValueAccessor, NgControl, ReactiveFormsModule } from '@angular/forms';
import { MatIcon } from '@angular/material/icon';

@Component({
    selector: 'app-text-input',
    standalone: true,
    imports: [MatIcon, ReactiveFormsModule, CommonModule],
    templateUrl: './text-input.html',
})
export class TextInput implements ControlValueAccessor {
    label = input<string>('');
    placeholder = input<string>('');
    type = input<string>('text');
    id = input<string>('');
    matIcon = input<string | undefined>();
    isPassword = input<boolean>(false);
    autocomplete = input<string>('off');

    showPassword = signal(false);

    private onChange: (value: any) => void = () => {};
    private onTouched: () => void = () => {};
    value: any = '';
    disabled = false;

    constructor(@Self() @Optional() public ngControl: NgControl) {
        if (this.ngControl) {
            this.ngControl.valueAccessor = this;
        }
    }

    get inputType(): string {
        if (this.isPassword()) {
            return this.showPassword() ? 'text' : 'password';
        }
        return this.type();
    }

    togglePassword(): void {
        this.showPassword.update((v) => !v);
    }

    get hasIcon(): boolean {
        return !!this.matIcon();
    }

    get errorMessage(): string {
        const control = this.ngControl?.control;

        if (!control?.touched || !control.invalid) {
            return '';
        }

        if (control.hasError('required')) {
            return `${this.label() || 'This field'} is required`;
        }

        if (control.hasError('email')) {
            return 'Please enter a valid email address';
        }

        if (control.hasError('minlength')) {
            const min = control.errors?.['minlength'].requiredLength;
            return `Minimum ${min} characters required`;
        }

        if (control.hasError('maxlength')) {
            const max = control.errors?.['maxlength'].requiredLength;
            return `Maximum ${max} characters allowed`;
        }

        if (control.hasError('mismatch')) {
            return 'Passwords do not match';
        }

        if (control.hasError('serverError')) {
            return control.getError('serverError');
        }

        return 'Invalid value';
    }

    get hasError(): boolean {
        const control = this.ngControl?.control;
        return !!(control?.touched && control?.invalid);
    }

    writeValue(value: any): void {
        this.value = value;
    }

    registerOnChange(fn: any): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.onTouched = fn;
    }

    setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }
}
