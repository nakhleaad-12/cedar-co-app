import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CheckoutComponent } from './checkout.component';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';
import { FormsModule } from '@angular/forms';
import { environment } from '../../../environments/environment';
import { of } from 'rxjs';

describe('CheckoutComponent', () => {
  let component: CheckoutComponent;
  let fixture: ComponentFixture<CheckoutComponent>;
  let httpMock: HttpTestingController;
  let router: Router;
  let authServiceMock: any;
  let toastServiceMock: any;

  beforeEach(async () => {
    authServiceMock = { isAdmin: false };
    toastServiceMock = { show: jasmine.createSpy('show') };

    await TestBed.configureTestingModule({
      declarations: [CheckoutComponent],
      imports: [HttpClientTestingModule, RouterTestingModule, FormsModule],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: ToastService, useValue: toastServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CheckoutComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    spyOn(router, 'navigate');
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should redirect to admin if the current user is an admin', () => {
    authServiceMock.isAdmin = true;
    component.ngOnInit();
    expect(router.navigate).toHaveBeenCalledWith(['/admin']);
  });

  it('should not redirect if user is not admin', () => {
    authServiceMock.isAdmin = false;
    component.ngOnInit();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  describe('placeOrder', () => {
    it('should place order successfully and show toast', () => {
      // Set form data
      component.street = '123 Main St';
      component.city = 'Beirut';
      component.region = 'Beirut';
      component.country = 'Lebanon';
      component.paymentMethod = 'CASH_ON_DELIVERY';
      component.coupon = '';
      
      const mockEvent = new Event('submit');
      spyOn(mockEvent, 'preventDefault');

      component.placeOrder(mockEvent);

      expect(mockEvent.preventDefault).toHaveBeenCalled();

      // Expect HTTP POST request
      const req = httpMock.expectOne(`${environment.apiUrl}/orders`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({
        shippingStreet: '123 Main St',
        shippingCity: 'Beirut',
        shippingRegion: 'Beirut',
        shippingCountry: 'Lebanon',
        paymentMethod: 'CASH_ON_DELIVERY',
        couponCode: null
      });

      // Flush success response
      req.flush({ success: true });

      // Check success state and toast
      expect(component.success).toBeTrue();
      expect(toastServiceMock.show).toHaveBeenCalledWith('Order placed successfully!', 'success');
    });

    it('should place order with coupon successfully', () => {
      component.coupon = 'SAVE20';
      const mockEvent = new Event('submit');
      component.placeOrder(mockEvent);

      const req = httpMock.expectOne(`${environment.apiUrl}/orders`);
      expect(req.request.body.couponCode).toBe('SAVE20');
      req.flush({ success: true });
    });

    it('should show error toast on failure', () => {
      const mockEvent = new Event('submit');
      component.placeOrder(mockEvent);

      const req = httpMock.expectOne(`${environment.apiUrl}/orders`);
      req.error(new ProgressEvent('error'));

      // Check error toast
      expect(component.success).toBeFalse();
      expect(toastServiceMock.show).toHaveBeenCalledWith('Failed to place order. Please try again.', 'error');
    });
  });
});
