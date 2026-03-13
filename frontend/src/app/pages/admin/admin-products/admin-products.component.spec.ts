import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AdminProductsComponent } from './admin-products.component';
import { ToastService } from '../../../services/toast.service';
import { environment } from '../../../../environments/environment';
import { FormsModule } from '@angular/forms';

describe('AdminProductsComponent', () => {
  let component: AdminProductsComponent;
  let fixture: ComponentFixture<AdminProductsComponent>;
  let httpMock: HttpTestingController;
  let toastServiceMock: any;

  const mockProduct = {
    id: 1,
    name: 'Test Product',
    slug: 'test-product',
    sku: 'TEST-SKU',
    price: 100,
    gender: 'UNISEX',
    active: true,
    images: [],
    sizes: ['M'],
    colors: ['Red'],
    stockMap: { 'M': 10 },
    featured: false,
    newArrival: false,
    bestSeller: false,
    rating: 0,
    reviewCount: 0
  };

  const mockPage = {
    content: [mockProduct],
    totalPages: 1,
    totalElements: 1
  };

  beforeEach(async () => {
    toastServiceMock = { show: jasmine.createSpy('show') };

    await TestBed.configureTestingModule({
      declarations: [AdminProductsComponent],
      imports: [HttpClientTestingModule, FormsModule],
      providers: [
        { provide: ToastService, useValue: toastServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminProductsComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create and load products on init', () => {
    const req = httpMock.expectOne(`${environment.apiUrl}/products?size=100&page=0`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);

    expect(component).toBeTruthy();
    expect(component.products.length).toBe(1);
    expect(component.loading).toBeFalse();
  });

  describe('autoSlug', () => {
    it('should generate a slug based on product name if slug is empty', () => {
      httpMock.expectOne(`${environment.apiUrl}/products?size=100&page=0`).flush(mockPage);

      component.form.name = 'Test Product!  Name';
      component.form.slug = '';
      
      component.autoSlug();

      expect(component.form.slug).toBe('test-product-name');
    });

    it('should not overwrite an existing slug', () => {
      httpMock.expectOne(`${environment.apiUrl}/products?size=100&page=0`).flush(mockPage);

      component.form.name = 'Test Product';
      component.form.slug = 'custom-slug';
      
      component.autoSlug();

      expect(component.form.slug).toBe('custom-slug');
    });
  });

  describe('createProduct', () => {
    it('should show error if required fields are missing', () => {
      httpMock.expectOne(`${environment.apiUrl}/products?size=100&page=0`).flush(mockPage);

      component.form.name = '';
      component.createProduct();

      expect(toastServiceMock.show).toHaveBeenCalledWith('Name, SKU and Price are required', 'error');
      httpMock.expectNone(`${environment.apiUrl}/admin/products`);
    });

    it('should successfully post valid product', () => {
      httpMock.expectOne(`${environment.apiUrl}/products?size=100&page=0`).flush(mockPage);

      component.form.name = 'New Product';
      component.form.sku = 'SKU-NEW';
      component.form.price = '50';
      component.form.sizes = 'S, M, L';
      component.form.colors = 'Red,Blue';
      component.form.imageUrl = 'new-product.jpg';
      
      component.createProduct();

      const req = httpMock.expectOne(`${environment.apiUrl}/admin/products`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(jasmine.objectContaining({
        name: 'New Product',
        slug: 'new-product',
        sku: 'SKU-NEW',
        price: 50,
        sizes: ['S', 'M', 'L'],
        colors: ['Red', 'Blue'],
        images: ['new-product.jpg'],
        stockMap: { 'S': 10, 'M': 10, 'L': 10 }
      }));

      const newProductMock = { ...mockProduct, id: 2, name: 'New Product' };
      req.flush(newProductMock);

      expect(component.products.length).toBe(2);
      expect(component.products[0].name).toBe('New Product');
      expect(toastServiceMock.show).toHaveBeenCalledWith('Product "New Product" created!', 'success');
      expect(component.showCreateForm).toBeFalse();
      expect(component.creating).toBeFalse();
    });
  });

  describe('deleteProduct', () => {
    it('should call DELETE and remove product when confirmed', () => {
      httpMock.expectOne(`${environment.apiUrl}/products?size=100&page=0`).flush(mockPage);

      spyOn(window, 'confirm').and.returnValue(true);
      
      const productToDelete: any = component.products[0];
      component.deleteProduct(productToDelete);

      const req = httpMock.expectOne(`${environment.apiUrl}/admin/products/1`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);

      expect(component.products.length).toBe(0);
      expect(toastServiceMock.show).toHaveBeenCalledWith('"Test Product" deleted', 'success');
    });

    it('should ignore if confirmation is cancelled', () => {
      httpMock.expectOne(`${environment.apiUrl}/products?size=100&page=0`).flush(mockPage);

      spyOn(window, 'confirm').and.returnValue(false);
      
      const productToDelete: any = component.products[0];
      component.deleteProduct(productToDelete);

      httpMock.expectNone(`${environment.apiUrl}/admin/products/1`);
      expect(component.products.length).toBe(1);
    });
  });
});
