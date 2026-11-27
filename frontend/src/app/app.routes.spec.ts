import { Routes } from '@angular/router';
import { routes } from './app.routes';
import { authGuard } from './core/auth/guards/auth.guard';
import { guestGuard } from './core/auth/guards/guest.guard';

describe('app routes', () => {
  const findRoute = (path: string): Routes[number] => {
    const route = routes.find((item) => item.path === path);
    expect(route).withContext(`route ${path}`).toBeDefined();
    return route!;
  };

  it('defines the public, auth, and fallback routes', () => {
    expect(routes.map((route) => route.path)).toEqual([
      '',
      'login',
      'register',
      'profile',
      'profile/account',
      'activity',
      'seller/dashboard',
      'products/new',
      'products/:id/edit',
      'products/list',
      'dashboard',
      'products/:id',
      'cart',
      'orders',
      'orders/:id',
      '**',
    ]);
  });

  it('redirects the account route and the catch-all', () => {
    expect(findRoute('profile/account').redirectTo).toBe('profile');
    expect(findRoute('**').redirectTo).toBe('');
  });

  it('protects private routes with the expected guards', () => {
    expect(findRoute('login').canActivate).toEqual([guestGuard]);
    expect(findRoute('register').canActivate).toEqual([guestGuard]);
    expect(findRoute('profile').canActivate).toEqual([authGuard]);
    expect(findRoute('dashboard').canActivate).toEqual([authGuard]);
    expect(findRoute('orders').canActivate).toEqual([authGuard]);
    expect(findRoute('orders/:id').canActivate).toEqual([authGuard]);
    expect(findRoute('activity').canActivate?.length).toBe(1);
    expect(findRoute('seller/dashboard').canActivate?.length).toBe(1);
    expect(findRoute('products/new').canActivate?.length).toBe(1);
    expect(findRoute('products/:id/edit').canActivate?.length).toBe(1);
    expect(findRoute('cart').canActivate?.length).toBe(1);
  });

  it('carries dashboard data', () => {
    expect(findRoute('dashboard').data).toEqual({ myProducts: true });
  });

  it('lazily loads every component', async () => {
    const lazyRoutes = routes.filter((route) => typeof route.loadComponent === 'function');

    expect(lazyRoutes.length).toBe(14);

    for (const route of lazyRoutes) {
      const component = await (route.loadComponent as () => Promise<unknown>)();
      expect(component).withContext(`component for ${route.path}`).toBeTruthy();
    }
  });
});
