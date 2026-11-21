import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { AuthResponse, User } from '../../models/user.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let router: Router;

  const user: User = {
    id: 'user-1',
    name: 'Ada Lovelace',
    email: 'ada@example.com',
    role: 'CLIENT',
  };

  const futureExpiry = () => new Date(Date.now() + 60 * 60 * 1000).toISOString();
  const pastExpiry = () => new Date(Date.now() - 60 * 60 * 1000).toISOString();

  const createService = (): AuthService => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));
    spyOn(router, 'navigateByUrl').and.returnValue(Promise.resolve(true));
    return TestBed.inject(AuthService);
  };

  beforeEach(() => {
    localStorage.clear();
    spyOn(console, 'error');
  });

  afterEach(() => {
    httpMock?.verify();
    localStorage.clear();
  });

  it('starts unauthenticated when no token is stored', () => {
    service = createService();

    expect(service.isAuthenticated).toBeFalse();
    expect(service.user).toBeNull();
    expect(service.getToken()).toBeNull();
    expect(service.getUserRole()).toBeNull();
    expect(service.isSeller).toBeFalse();
    expect(service.isClient).toBeFalse();
  });

  it('logs in, stores the token, and loads the current user', () => {
    service = createService();
    let result: User | null | undefined;

    service.login({ email: 'ada@example.com', password: 'secret' }).subscribe((value) => (result = value));

    const loginRequest = httpMock.expectOne('/users/auth/login');
    expect(loginRequest.request.method).toBe('POST');
    expect(loginRequest.request.body).toEqual({ email: 'ada@example.com', password: 'secret' });
    loginRequest.flush({ token: 'token-1', expiresAt: futureExpiry() } satisfies AuthResponse);

    const meRequest = httpMock.expectOne('/users/me');
    expect(meRequest.request.method).toBe('GET');
    meRequest.flush(user);

    expect(result).toEqual(user);
    expect(service.user).toEqual(user);
    expect(service.isAuthenticated).toBeTrue();
    expect(service.isClient).toBeTrue();
    expect(service.isSeller).toBeFalse();
    expect(service.getUserRole()).toBe('CLIENT');
    expect(localStorage.getItem('auth_token')).toBe('token-1');
    expect(service.getExpiresAt()).toBeTruthy();
  });

  it('propagates and logs login errors', () => {
    service = createService();
    let error: unknown;

    service.login({ email: 'ada@example.com', password: 'wrong' }).subscribe({
      error: (err) => (error = err),
    });

    httpMock
      .expectOne('/users/auth/login')
      .flush({ message: 'nope' }, { status: 401, statusText: 'Unauthorized' });

    expect(error).toBeTruthy();
    expect(console.error).toHaveBeenCalled();
  });

  it('fetches the current user and logs failures', () => {
    service = createService();
    let result: User | null | undefined;

    service.getCurrentUser().subscribe((value) => (result = value));
    httpMock.expectOne('/users/me').flush(user);
    expect(result).toEqual(user);

    let error: unknown;
    service.getCurrentUser().subscribe({ error: (err) => (error = err) });
    httpMock.expectOne('/users/me').flush({}, { status: 500, statusText: 'Server Error' });

    expect(error).toBeTruthy();
    expect(console.error).toHaveBeenCalled();
  });

  it('registers with an avatar and redirects to the login page', () => {
    service = createService();
    const avatar = new File(['avatar'], 'avatar.png', { type: 'image/png' });
    let result: AuthResponse | undefined;

    service
      .register({
        name: 'Ada Lovelace',
        email: 'ada@example.com',
        password: 'secret',
        role: 'SELLER',
        avatar,
      })
      .subscribe((value) => (result = value));

    const request = httpMock.expectOne('/users/auth/register');
    expect(request.request.method).toBe('POST');
    const body = request.request.body as FormData;
    expect(body instanceof FormData).toBeTrue();
    expect(body.get('name')).toBe('Ada Lovelace');
    expect(body.get('email')).toBe('ada@example.com');
    expect(body.get('password')).toBe('secret');
    expect(body.get('role')).toBe('SELLER');
    expect(body.get('avatar')).toBe(avatar);

    const response: AuthResponse = { token: 'token-2', expiresAt: futureExpiry() };
    request.flush(response);

    expect(result).toEqual(response);
    expect(router.navigateByUrl).toHaveBeenCalledWith('/login', {
      state: { email: 'ada@example.com' },
    });
  });

  it('registers without an avatar', () => {
    service = createService();

    service
      .register({
        name: 'Ada Lovelace',
        email: 'ada@example.com',
        password: 'secret',
        role: 'CLIENT',
      })
      .subscribe();

    const request = httpMock.expectOne('/users/auth/register');
    const body = request.request.body as FormData;
    expect(body.get('avatar')).toBeNull();
    request.flush({ token: 'token-3', expiresAt: futureExpiry() });
  });

  it('propagates and logs registration errors', () => {
    service = createService();
    let error: unknown;

    service
      .register({
        name: 'Ada Lovelace',
        email: 'ada@example.com',
        password: 'secret',
        role: 'CLIENT',
      })
      .subscribe({ error: (err) => (error = err) });

    httpMock
      .expectOne('/users/auth/register')
      .flush({ message: 'taken' }, { status: 409, statusText: 'Conflict' });

    expect(error).toBeTruthy();
    expect(console.error).toHaveBeenCalled();
  });

  it('updates the current user and exposes role helpers', () => {
    service = createService();
    const seller: User = { ...user, role: 'SELLER' };

    service.updateCurrentUser(seller);

    expect(service.user).toEqual(seller);
    expect(service.isSeller).toBeTrue();
    expect(service.isClient).toBeFalse();
    expect(service.getUserRole()).toBe('SELLER');

    service.currentUser$.subscribe((value) => expect(value).toEqual(seller));
  });

  it('logs out, clears stored credentials, and redirects', () => {
    service = createService();
    localStorage.setItem('auth_token', 'token-1');
    localStorage.setItem('auth_expires_at', futureExpiry());

    service.logout();

    expect(localStorage.getItem('auth_token')).toBeNull();
    expect(localStorage.getItem('auth_expires_at')).toBeNull();
    expect(service.isAuthenticated).toBeFalse();
    expect(service.user).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('returns the stored token while it is still valid', () => {
    service = createService();
    localStorage.setItem('auth_token', 'token-1');
    localStorage.setItem('auth_expires_at', futureExpiry());

    expect(service.getToken()).toBe('token-1');
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('expires a token whose expiration has passed', () => {
    service = createService();
    localStorage.setItem('auth_token', 'token-1');
    localStorage.setItem('auth_expires_at', pastExpiry());

    expect(service.getToken()).toBeNull();
    expect(localStorage.getItem('auth_token')).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('restores a valid session during initialization', () => {
    localStorage.setItem('auth_token', 'token-1');
    localStorage.setItem('auth_expires_at', futureExpiry());

    service = createService();

    const meRequest = httpMock.expectOne('/users/me');
    meRequest.flush(user);

    expect(service.user).toEqual(user);
    expect(service.isAuthenticated).toBeTrue();
  });

  it('logs out during initialization when the stored token has expired', () => {
    localStorage.setItem('auth_token', 'token-1');
    localStorage.setItem('auth_expires_at', pastExpiry());

    service = createService();

    httpMock.expectNone('/users/me');
    expect(service.isAuthenticated).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('logs out when initialization detects an expired session', () => {
    service = createService();
    localStorage.setItem('auth_expires_at', pastExpiry());
    spyOn(service, 'getToken').and.returnValue('token-1');

    (service as any).initializeAuth();

    expect(service.isAuthenticated).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('clears auth when restoring the session throws synchronously', () => {
    service = createService();
    localStorage.setItem('auth_token', 'token-1');
    localStorage.setItem('auth_expires_at', futureExpiry());
    spyOn(service, 'getCurrentUser').and.throwError('boom');

    (service as any).initializeAuth();

    expect(localStorage.getItem('auth_token')).toBeNull();
    expect(service.isAuthenticated).toBeFalse();
  });
});
