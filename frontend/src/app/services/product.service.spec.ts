import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ProductService, Product, PageResult } from './product.service';
import { environment } from '../../environments/environment';

describe('ProductService', () => {
  let service: ProductService;
  let httpMock: HttpTestingController;

  const mockProduct: Product = {
    id: 1,
    name: 'Test Product',
    slug: 'test-product',
    description: 'A great product',
    price: 100,
    salePrice: null,
    categoryName: 'Category',
    collectionName: 'Collection',
    collectionSlug: 'collection',
    gender: 'Unisex',
    images: ['img1.png'],
    sizes: ['M'],
    colors: ['Red'],
    stockMap: { 'Red-M': 10 },
    featured: true,
    newArrival: false,
    bestSeller: false,
    rating: 4.5,
    reviewCount: 10,
    createdAt: '2023-01-01T00:00:00Z'
  };

  const mockPage: PageResult<Product> = {
    content: [mockProduct],
    totalElements: 1,
    totalPages: 1,
    number: 0,
    size: 12
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ProductService]
    });
    service = TestBed.inject(ProductService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getAll', () => {
    it('should return a page of products with default params', () => {
      service.getAll().subscribe(page => {
        expect(page).toEqual(mockPage);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/products?page=0&size=12`);
      expect(req.request.method).toBe('GET');
      req.flush(mockPage);
    });

    it('should append sort param if provided', () => {
      service.getAll(1, 24, 'price,asc').subscribe(page => {
        expect(page).toEqual(mockPage);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/products?page=1&size=24&sort=price,asc`);
      expect(req.request.method).toBe('GET');
      req.flush(mockPage);
    });
  });

  describe('getBySlug', () => {
    it('should return product by slug', () => {
      service.getBySlug('test-product').subscribe(product => {
        expect(product).toEqual(mockProduct);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/products/test-product`);
      expect(req.request.method).toBe('GET');
      req.flush(mockProduct);
    });
  });

  describe('getFeatured', () => {
    it('should return featured products', () => {
      service.getFeatured().subscribe(products => {
        expect(products).toEqual([mockProduct]);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/products/featured`);
      expect(req.request.method).toBe('GET');
      req.flush([mockProduct]);
    });
  });

  describe('getNewArrivals', () => {
    it('should return new arrivals', () => {
      service.getNewArrivals().subscribe(products => {
        expect(products).toEqual([mockProduct]);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/products/new-arrivals`);
      expect(req.request.method).toBe('GET');
      req.flush([mockProduct]);
    });
  });

  describe('getBestSellers', () => {
    it('should return best sellers', () => {
      service.getBestSellers().subscribe(products => {
        expect(products).toEqual([mockProduct]);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/products/best-sellers`);
      expect(req.request.method).toBe('GET');
      req.flush([mockProduct]);
    });
  });
  
  describe('getProductReviews', () => {
    it('should return product reviews', () => {
      const mockReviews = [{ id: 1, title: 'Great', body: 'Very good' }];
      service.getProductReviews(1).subscribe(reviews => {
        expect(reviews).toEqual(mockReviews);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/products/1/reviews`);
      expect(req.request.method).toBe('GET');
      req.flush(mockReviews);
    });
  });
  
  describe('addReview', () => {
    it('should add a review to product', () => {
      const payload = { rating: 5, title: 'Awesome', body: 'Loved it' };
      service.addReview(1, payload).subscribe(res => {
        expect(res).toBeTruthy();
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/products/1/reviews`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(payload);
      req.flush({ success: true });
    });
  });
});
