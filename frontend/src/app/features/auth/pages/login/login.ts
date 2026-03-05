import { Component, inject, signal } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { AuthStore } from '../../../../core/auth/auth.store';

@Component({
  selector: 'app-login',
  imports: [],
  templateUrl: './login.html',
  styles: ``,
})
export class Login {
    private readonly fb = inject(FormBuilder);
    readonly authStore = inject(AuthStore);
    
    showPassword = signal(false);
    
    loginForm = this.fb.group({
        username: [''],
        password: [''],
    })
}
