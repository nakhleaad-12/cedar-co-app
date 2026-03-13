import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AdminDashboardComponent } from './admin-dashboard.component';
import { environment } from '../../../../environments/environment';

describe('AdminDashboardComponent', () => {
  let component: AdminDashboardComponent;
  let fixture: ComponentFixture<AdminDashboardComponent>;
  let httpMock: HttpTestingController;

  const mockStats = {
    totalProducts: 150,
    totalOrders: 42,
    totalCustomers: 89,
    totalRevenue: 15430.50
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AdminDashboardComponent],
      imports: [HttpClientTestingModule]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminDashboardComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    const req = httpMock.expectOne(`${environment.apiUrl}/admin/dashboard/stats`);
    req.flush(mockStats);
    expect(component).toBeTruthy();
  });

  it('should fetch stats and map to statCards on init', () => {
    const req = httpMock.expectOne(`${environment.apiUrl}/admin/dashboard/stats`);
    expect(req.request.method).toBe('GET');
    
    req.flush(mockStats);

    expect(component.stats).toEqual(mockStats);
    expect(component.statCards.length).toBe(4);
    
    expect(component.statCards[0]).toEqual({ label: 'Total Products', value: 150 });
    expect(component.statCards[1]).toEqual({ label: 'Total Orders', value: 42 });
    expect(component.statCards[2]).toEqual({ label: 'Customers', value: 89 });
    expect(component.statCards[3]).toEqual({ label: 'Revenue', value: '$15430.50' });
  });
});
