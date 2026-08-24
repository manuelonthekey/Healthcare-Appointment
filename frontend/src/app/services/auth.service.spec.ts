import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';

describe('AuthService & Auth Flow', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should process a successful login', () => {
    const dummyResponse = { token: 'fake-jwt-token', role: 'PATIENT', email: 'test@test.com' };
    
    service.login('test@test.com', 'password').subscribe(res => {
      expect(res.token).toEqual('fake-jwt-token');
      expect(localStorage.getItem('token')).toEqual('fake-jwt-token');
      expect(service.hasRole('PATIENT')).toBeTrue();
    });

    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush(dummyResponse);
  });

  it('should handle a failed login', () => {
    service.login('wrong@test.com', 'badpass').subscribe({
      error: err => expect(err.status).toBe(401)
    });
    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
    req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
  });

  it('should process a logout correctly', () => {
    localStorage.setItem('token', 'old-token');
    service.logout();
    expect(localStorage.getItem('token')).toBeNull();
    expect(service.currentUserValue).toBeNull();
  });

  it('should enforce role authorization logic', () => {
    localStorage.setItem('token', 'some-token');
    localStorage.setItem('role', 'DOCTOR');
    const localService = new AuthService(TestBed.inject(HttpClientTestingModule) as any);
    
    expect(localService.hasRole('DOCTOR')).toBeTrue();
    expect(localService.hasRole('ADMIN')).toBeFalse();
  });
});
