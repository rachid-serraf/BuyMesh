import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';

describe('authGuard', () => {
  let authService: { isAuthenticated: boolean };
  let router: { navigate: jasmine.Spy };

  beforeEach(() => {
    authService = { isAuthenticated: false };
    router = { navigate: jasmine.createSpy('navigate') };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
      ],
    });
  });

  it('allows authenticated users', () => {
    authService.isAuthenticated = true;

    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as any, { url: '/orders' } as any),
    );

    expect(result).toBeTrue();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('redirects unauthenticated users to login with the return url', () => {
    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as any, { url: '/orders/1' } as any),
    );

    expect(result).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/login'], {
      queryParams: { returnUrl: '/orders/1' },
    });
  });
});
