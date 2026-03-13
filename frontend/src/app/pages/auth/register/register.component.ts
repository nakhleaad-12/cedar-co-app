import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['../login/login.component.scss']
})
export class RegisterComponent {
  form = this.fb.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    phone: ['']
  });
  loading = false;
  error = '';

  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router) {}

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.error = '';
    const { firstName, lastName, email, password, phone } = this.form.value;
    this.auth.register({ firstName: firstName!, lastName: lastName!, email: email!, password: password!, phone: phone || '' }).subscribe({
      next: () => this.router.navigate(['/']),
      error: (e) => { this.error = e.error?.message || 'Registration failed.'; this.loading = false; }
    });
  }
}
