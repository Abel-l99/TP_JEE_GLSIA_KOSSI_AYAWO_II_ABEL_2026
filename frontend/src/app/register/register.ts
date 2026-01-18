// src/app/pages/register.component.ts
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouteReuseStrategy, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { LoginService } from '../services/login/login-service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class Register {
  
  private formBuilder = inject(FormBuilder);
  private loginService = inject(LoginService);
  private cdr = inject(ChangeDetectorRef);
  private router = inject(Router);

  registerFormGroup: FormGroup = this.formBuilder.group({
    username: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required]]
  }, { validators: this.passwordMatchValidator });

  private passwordMatchValidator(form: FormGroup) {
    const password = form.get('password')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  invalidCredentials = false;
  protected errorMessage: string | null = null;

  onSubmit() {
    if (this.registerFormGroup.valid) {
      this.invalidCredentials = false;

      const userData = {
        username: this.registerFormGroup.value.username,
        password: this.registerFormGroup.value.password,
        role: 'CLIENT'
      };

      this.loginService.register(userData).subscribe({
        next: (response) => {
          console.log('Inscription réussie:', response);
          this.router.navigate(['/login']);
        },
        error: (error) => {
          this.invalidCredentials = true;
          this.errorMessage = error.error?.message || 'Erreur inconnue';
          console.log(error);
          this.cdr.detectChanges();
        }
      });
    } else {
      this.registerFormGroup.markAllAsTouched();
    }
  }
  
}