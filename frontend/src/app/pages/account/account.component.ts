import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService, AuthUser } from '../../services/auth.service';
import { UserService, UserProfile, Address } from '../../services/user.service';
import { ToastService } from '../../services/toast.service';
import { environment } from '../../../environments/environment';

@Component({ 
  selector: 'app-account', 
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './account.component.html',
  styleUrls: ['./account.component.scss']
})
export class AccountComponent implements OnInit {
  user: UserProfile | null = null;
  orders: any[] = [];
  
  // Forms
  profileForm: FormGroup;
  passwordForm: FormGroup;
  addressForm: FormGroup;

  // Modals state
  showProfileModal = false;
  showPasswordModal = false;
  showAddressModal = false;
  editingAddressId: number | null = null;

  constructor(
    private auth: AuthService, 
    private userService: UserService,
    private toast: ToastService,
    private fb: FormBuilder,
    private http: HttpClient
  ) {
    this.profileForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      phone: ['']
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validator: this.passwordMatchValidator });

    this.addressForm = this.fb.group({
      street: ['', Validators.required],
      city: ['', Validators.required],
      state: ['', Validators.required],
      zipCode: ['', Validators.required],
      country: ['Lebanon', Validators.required],
      isDefault: [false]
    });
  }

  ngOnInit(): void {
    this.loadProfile();
    this.loadOrders();
  }

  loadProfile(): void {
    this.userService.getProfile().subscribe({
      next: p => {
        this.user = p;
        this.profileForm.patchValue({
          firstName: p.firstName,
          lastName: p.lastName,
          phone: p.phone
        });
      },
      error: err => this.toast.show('Failed to load profile', 'error')
    });
  }

  loadOrders(): void {
    this.http.get<any[]>(`${environment.apiUrl}/orders`).subscribe({
      next: o => this.orders = o,
      error: err => console.error('Failed to fetch orders:', err)
    });
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('newPassword')?.value === g.get('confirmPassword')?.value 
      ? null : { mismatch: true };
  }

  // Profile Actions
  updateProfile() {
    if (this.profileForm.invalid) return;
    this.userService.updateProfile(this.profileForm.value).subscribe({
      next: () => {
        this.toast.show('Profile updated successfully');
        this.showProfileModal = false;
        this.loadProfile();
      },
      error: () => this.toast.show('Failed to update profile', 'error')
    });
  }

  changePassword() {
    if (this.passwordForm.invalid) return;
    this.userService.changePassword(this.passwordForm.value).subscribe({
      next: () => {
        this.toast.show('Password changed successfully');
        this.showPasswordModal = false;
        this.passwordForm.reset();
      },
      error: (err) => this.toast.show(err.error?.message || 'Failed to change password', 'error')
    });
  }

  // Address Actions
  openAddressModal(address?: Address) {
    if (address) {
      this.editingAddressId = address.id || null;
      this.addressForm.patchValue(address);
    } else {
      this.editingAddressId = null;
      this.addressForm.reset({ country: 'Lebanon', isDefault: false });
    }
    this.showAddressModal = true;
  }

  saveAddress() {
    if (this.addressForm.invalid) return;
    const obs = this.editingAddressId 
      ? this.userService.updateAddress(this.editingAddressId, this.addressForm.value)
      : this.userService.addAddress(this.addressForm.value);

    obs.subscribe({
      next: () => {
        this.toast.show(`Address ${this.editingAddressId ? 'updated' : 'added'} successfully`);
        this.showAddressModal = false;
        this.loadProfile();
      },
      error: () => this.toast.show('Failed to save address', 'error')
    });
  }

  deleteAddress(id: number, event: Event) {
    event.stopPropagation();
    if (confirm('Are you sure you want to delete this address?')) {
      this.userService.deleteAddress(id).subscribe({
        next: () => {
          this.toast.show('Address deleted');
          this.loadProfile();
        },
        error: () => this.toast.show('Failed to delete address', 'error')
      });
    }
  }
}
