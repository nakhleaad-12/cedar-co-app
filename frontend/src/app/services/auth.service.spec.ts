import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService, AuthUser } from './auth.service';
import { environment } from '../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const mockUser: AuthUser = {
    email: 'test@example.com',
    firstName: 'Test',
    lastName: 'User',
    role: 'USER',
    accessToken: 'access-token-123',
    refreshToken: 'refresh-token-123'
  };

  const mockAdmin: AuthUser = {
    ...mockUser,
    role: 'ADMIN'
  };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('Initial State', () => {
    it('should initialize with no user if local storage is empty', () => {
      expect(service.currentUser).toBeNull();
      expect(service.isLoggedIn).toBeFalse();
      expect(service.isAdmin).toBeFalse();
      expect(service.getToken()).toBeNull();
    });

    it('should initialize with user from local storage', () => {
      localStorage.setItem('cedar_user', JSON.stringify(mockUser));
      // Re-initialize to trigger loadUser
      const newService = new AuthService(TestBed.inject(HttpClientTestingModule) as any);
      expect(newService.currentUser).toEqual(mockUser);
      expect(newService.isLoggedIn).toBeTrue();
      expect(newService.isAdmin).toBeFalse();
      expect(newService.getToken()).toBe('access-token-123');
    });

    it('should initialize with admin from local storage', () => {
      localStorage.setItem('cedar_user', JSON.stringify(mockAdmin));
      const newService = new AuthService(TestBed.inject(HttpClientTestingModule) as any);
      expect(newService.currentUser).toEqual(mockAdmin);
      expect(newService.isLoggedIn).toBeTrue();
      expect(newService.isAdmin).toBeTrue();
    });
  });

  describe('login', () => {
    it('should send login request and save user on success', () => {
      service.login('test@example.com', 'password').subscribe(user => {
        expect(user).toEqual(mockUser);
        expect(service.currentUser).toEqual(mockUser);
        expect(localStorage.getItem('cedar_user')).toBe(JSON.stringify(mockUser));
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ email: 'test@example.com', password: 'password' });
      req.flush(mockUser);
    });
  });

  describe('register', () => {
    it('should send register request and save user on success', () => {
      const payload = {
        firstName: 'New',
        lastName: 'User',
        email: 'new@example.com',
        password: 'password'
      };

      service.register(payload).subscribe(user => {
        expect(user).toEqual(mockUser);
        expect(service.currentUser).toEqual(mockUser);
        expect(localStorage.getItem('cedar_user')).toBe(JSON.stringify(mockUser));
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/auth/register`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(payload);
      req.flush(mockUser);
    });
  });

  describe('logout', () => {
    it('should clear local storage and current user', () => {
      // Setup
      localStorage.setItem('cedar_user', JSON.stringify(mockUser));
      const newService = new AuthService(TestBed.inject(HttpClientTestingModule) as any);
      expect(newService.isLoggedIn).toBeTrue();

      // Action
      newService.logout();

      // Assert
      expect(localStorage.getItem('cedar_user')).toBeNull();
      expect(newService.currentUser).toBeNull();
      expect(newService.isLoggedIn).toBeFalse();
    });
  });
});
