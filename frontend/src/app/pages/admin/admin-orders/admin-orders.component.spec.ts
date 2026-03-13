import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AdminOrdersComponent } from './admin-orders.component';
import { ToastService } from '../../../services/toast.service';
import { environment } from '../../../../environments/environment';

describe('AdminOrdersComponent', () => {
  let component: AdminOrdersComponent;
  let fixture: ComponentFixture<AdminOrdersComponent>;
  let httpMock: HttpTestingController;
  let toastServiceMock: any;

  const mockOrder = {
    id: 1,
    userEmail: 'test@example.com',
    createdAt: '2023-01-01T00:00:00Z',
    subtotal: 100,
    discount: 0,
    total: 100,
    status: 'PENDING',
    paymentStatus: 'PAID',
    paymentMethod: 'CREDIT_CARD',
    shippingCity: 'Beirut',
    shippingCountry: 'Lebanon',
    items: [{ productName: 'Test Item', size: 'M', color: 'Red', quantity: 1, unitPrice: 100 }],
    updatingStatus: false
  };

  const mockOrderPage = {
    content: [mockOrder],
    totalPages: 5,
    totalElements: 75
  };

  beforeEach(async () => {
    toastServiceMock = { show: jasmine.createSpy('show') };

    await TestBed.configureTestingModule({
      declarations: [AdminOrdersComponent],
      imports: [HttpClientTestingModule],
      providers: [
        { provide: ToastService, useValue: toastServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminOrdersComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create and load orders on init', () => {
    const req = httpMock.expectOne(`${environment.apiUrl}/admin/orders?page=0&size=15`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);

    expect(component).toBeTruthy();
    expect(component.orders.length).toBe(1);
    expect(component.orders[0].updatingStatus).toBeFalse();
    expect(component.totalPages).toBe(5);
    expect(component.totalElements).toBe(75);
    expect(component.loading).toBeFalse();
  });

  it('should handle order loading error gracefully', () => {
    const req = httpMock.expectOne(`${environment.apiUrl}/admin/orders?page=0&size=15`);
    req.error(new ProgressEvent('error'));

    expect(component.loading).toBeFalse();
    expect(toastServiceMock.show).toHaveBeenCalledWith('Failed to load orders', 'error');
  });

  describe('Pagination', () => {
    it('should navigate next page and reload', () => {
      // First, handle the init request
      httpMock.expectOne(`${environment.apiUrl}/admin/orders?page=0&size=15`).flush(mockPage);
      
      component.totalPages = 5;
      component.nextPage();

      expect(component.page).toBe(1);
      const req = httpMock.expectOne(`${environment.apiUrl}/admin/orders?page=1&size=15`);
      req.flush(mockPage);
    });

    it('should not navigate next page if at last page', () => {
      httpMock.expectOne(`${environment.apiUrl}/admin/orders?page=0&size=15`).flush(mockPage);
      
      component.page = 4;
      component.totalPages = 5;
      component.nextPage();

      httpMock.expectNone(`${environment.apiUrl}/admin/orders?page=5&size=15`);
      expect(component.page).toBe(4);
    });

    it('should navigate prev page and reload', () => {
      httpMock.expectOne(`${environment.apiUrl}/admin/orders?page=0&size=15`).flush(mockPage);
      
      component.page = 1;
      component.prevPage();

      expect(component.page).toBe(0);
      const req = httpMock.expectOne(`${environment.apiUrl}/admin/orders?page=0&size=15`);
      req.flush(mockPage);
    });
  });

  describe('updateStatus', () => {
    it('should update order status successfully', () => {
      httpMock.expectOne(`${environment.apiUrl}/admin/orders?page=0&size=15`).flush(mockPage);

      const orderToUpdate = { ...component.orders[0] };
      component.updateStatus(orderToUpdate as any, 'SHIPPED');

      expect(orderToUpdate.updatingStatus).toBeTrue();

      const req = httpMock.expectOne(`${environment.apiUrl}/admin/orders/1/status`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({ status: 'SHIPPED' });

      req.flush({ status: 'SHIPPED' });

      expect(orderToUpdate.status).toBe('SHIPPED');
      expect(orderToUpdate.updatingStatus).toBeFalse();
      expect(toastServiceMock.show).toHaveBeenCalledWith('Order #1 → SHIPPED', 'success');
    });

    it('should ignore if status is the same', () => {
      httpMock.expectOne(`${environment.apiUrl}/admin/orders?page=0&size=15`).flush(mockPage);
      
      const order = { ...component.orders[0] };
      component.updateStatus(order as any, 'PENDING');
      
      httpMock.expectNone(`${environment.apiUrl}/admin/orders/1/status`);
    });

    it('should handle update status error', () => {
      httpMock.expectOne(`${environment.apiUrl}/admin/orders?page=0&size=15`).flush(mockPage);

      const orderToUpdate = { ...component.orders[0] };
      component.updateStatus(orderToUpdate as any, 'SHIPPED');

      const req = httpMock.expectOne(`${environment.apiUrl}/admin/orders/1/status`);
      req.error(new ProgressEvent('error'));

      expect(orderToUpdate.updatingStatus).toBeFalse();
      expect(toastServiceMock.show).toHaveBeenCalledWith('Failed to update status', 'error');
    });
  });
});

const mockPage = {
    content: [{
        id: 1,
        userEmail: 'test@example.com',
        createdAt: '2023-01-01T00:00:00Z',
        subtotal: 100,
        discount: 0,
        total: 100,
        status: 'PENDING',
        paymentStatus: 'PAID',
        paymentMethod: 'CREDIT_CARD',
        shippingCity: 'Beirut',
        shippingCountry: 'Lebanon',
        items: [{ productName: 'Test Item', size: 'M', color: 'Red', quantity: 1, unitPrice: 100 }]
    }],
    totalPages: 5,
    totalElements: 75
};
