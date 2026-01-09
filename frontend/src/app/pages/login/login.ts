import { CommonModule } from '@angular/common';
import { Component, inject, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { LoginService } from '../../services/login/login-service';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { Credentials } from '../../models/credentials.model';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-login',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login implements OnDestroy {

  private formBuilder = inject(FormBuilder);
  private loginService = inject(LoginService);
  private router = inject(Router);

  private  loginSubscription: Subscription | null = null;

  loginFormGroup: FormGroup = this.formBuilder.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]]
  });

  invalidCredentials = false;

  login(){
    this.loginSubscription = this.loginService.login(
      this.loginFormGroup.value as Credentials
    ).subscribe({
      next:(result:User | null | undefined) => {
        this.navigateHome();
      },
      error: error =>  {
        console.log(error);
        this.invalidCredentials = true;
      }
    })
  }

  navigateHome(){
    this.router.navigate(['dashboard']);
  }

  ngOnDestroy(): void {
    this.loginSubscription?.unsubscribe();
  }

}
