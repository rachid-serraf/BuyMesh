import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { guestGuard } from './guest.guard';
import { AuthService } from '../services/auth.service';

describe('guestGuard', () => {
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

  it('allows guests', () => {
    const result = TestBed.runInInjectionContext(() =>
      guestGuard({} as any, { url: '/login' } as any),
    );

    expect(result).toBeTrue();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('redirects authenticated users home', () => {
    authService.isAuthenticated = true;

    const result = TestBed.runInInjectionContext(() =>
      guestGuard({} as any, { url: '/login' } as any),
    );

    expect(result).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });
});
