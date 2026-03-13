import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CartService, Cart } from './cart.service';
import { environment } from '../../environments/environment';

describe('CartService', () => {
  let service: CartService;
  let httpMock: HttpTestingController;

  const mockCart: Cart = {
    id: 1,
    items: [
      {
        id: 101,
        productId: 201,
        productName: 'Test Product',
        productSlug: 'test-product',
        imageUrl: 'test.jpg',
        price: 100,
        salePrice: null,
        size: 'L',
        color: 'Red',
        quantity: 2,
        subtotal: 200
      },
      {
        id: 102,
        productId: 202,
        productName: 'Another Product',
        productSlug: 'another-product',
        imageUrl: 'another.jpg',
        price: 50,
        salePrice: 40,
        size: 'M',
        color: 'Blue',
        quantity: 1,
        subtotal: 40
      }
    ],
    total: 240
  };

  const mockEmptyCart: Cart = {
    id: 1,
    items: [],
    total: 0
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CartService]
    });
    service = TestBed.inject(CartService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('Initial State', () => {
    it('should initialize with item count 0', () => {
      expect(service.itemCount).toBe(0);
    });
  });

  describe('loadCart', () => {
    it('should load cart and update behavior subject', () => {
      service.loadCart().subscribe(cart => {
        expect(cart).toEqual(mockCart);
        expect(service.itemCount).toBe(3); // 2 + 1
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/cart`);
      expect(req.request.method).toBe('GET');
      req.flush(mockCart);
    });
  });

  describe('addItem', () => {
    it('should add item to cart and update behavior subject', () => {
      const payload = { productId: 201, size: 'L', color: 'Red', quantity: 2 };
      
      service.addItem(payload.productId, payload.size, payload.color, payload.quantity).subscribe(cart => {
        expect(cart).toEqual(mockCart);
        expect(service.itemCount).toBe(3);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/cart/items`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(payload);
      req.flush(mockCart);
    });

    it('should use default quantity of 1 if not provided', () => {
      service.addItem(201, 'L', 'Red').subscribe();

      const req = httpMock.expectOne(`${environment.apiUrl}/cart/items`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ productId: 201, size: 'L', color: 'Red', quantity: 1 });
      req.flush(mockCart);
    });
  });

  describe('updateItem', () => {
    it('should update item quantity in cart', () => {
      const payload = { quantity: 5 };
      
      service.updateItem(101, payload.quantity).subscribe(cart => {
        expect(cart).toEqual(mockCart);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/cart/items/101`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(payload);
      req.flush(mockCart);
    });
  });

  describe('removeItem', () => {
    it('should remove item from cart', () => {
      service.removeItem(101).subscribe(cart => {
        expect(cart).toEqual(mockCart);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/cart/items/101`);
      expect(req.request.method).toBe('DELETE');
      req.flush(mockCart);
    });
  });

  describe('clearCart', () => {
    it('should clear cart and set subject to null', () => {
      // Pre-populate so we can test the subject clearing
      service.loadCart().subscribe();
      let req = httpMock.expectOne(`${environment.apiUrl}/cart`);
      req.flush(mockCart);
      expect(service.itemCount).toBe(3);

      service.clearCart().subscribe(() => {
        expect(service.itemCount).toBe(0);
      });

      req = httpMock.expectOne(`${environment.apiUrl}/cart`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });
});
