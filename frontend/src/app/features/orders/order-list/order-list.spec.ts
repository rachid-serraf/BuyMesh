import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BehaviorSubject, of, throwError } from 'rxjs';

import { AuthService } from '../../../core/auth/services/auth.service';
import { Order, OrderPage, OrderStatus } from '../../../core/models/order.model';
import { Role, User } from '../../../core/models/user.model';
import { OrderService } from '../../../core/services/order.service';
import { OrderList } from './order-list';

describe('OrderList', () => {
  let fixture: ComponentFixture<OrderList>;
  let component: OrderList;

  let currentUser$: BehaviorSubject<User | null>;
  let authStub: { user: User | null; currentUser$: BehaviorSubject<User | null> };
  let orderService: jasmine.SpyObj<OrderService>;
  let dialog: jasmine.SpyObj<MatDialog>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;

  const sampleOrder = (overrides: Partial<Order> = {}): Order =>
    ({
      id: 'ord-1',
      status: 'PENDING' as OrderStatus,
      createdAt: '2026-11-01T10:00:00Z',
      total: 45,
      items: [],
      ...overrides,
    }) as Order;

  const toPage = (content: Order[], page = 0, size = 10): OrderPage =>
    ({
      content,
      totalElements: content.length,
      page,
      size,
    }) as OrderPage;

  const el = () => fixture.nativeElement as HTMLElement;

  function setRole(role: Role | null, id = 'user-1'): void {
    const user = role ? ({ id, role } as User) : null;
    authStub.user = user;
    currentUser$.next(user);
  }

  function mount(role: Role | null = 'CLIENT'): void {
    setRole(role);
    fixture = TestBed.createComponent(OrderList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  beforeEach(() => {
    currentUser$ = new BehaviorSubject<User | null>({ id: 'user-1', role: 'CLIENT' } as User);
    authStub = {
      user: { id: 'user-1', role: 'CLIENT' } as User,
      currentUser$,
    };

    orderService = jasmine.createSpyObj<OrderService>('OrderService', [
      'getMyOrders',
      'getSellerOrders',
      'cancelOrder',
      'redoOrder',
      'removeOrder',
      'updateStatus',
    ]);
    orderService.getMyOrders.and.returnValue(of(toPage([sampleOrder()])));
    orderService.getSellerOrders.and.returnValue(of(toPage([sampleOrder({ id: 'ord-seller' })])));
    orderService.cancelOrder.and.returnValue(of(void 0));
    orderService.redoOrder.and.returnValue(of(void 0));
    orderService.removeOrder.and.returnValue(of(void 0));
    orderService.updateStatus.and.returnValue(of(void 0));

    dialog = jasmine.createSpyObj<MatDialog>('MatDialog', ['open']);
    dialog.open.and.returnValue({ afterClosed: () => of(true) } as never);

    snackBar = jasmine.createSpyObj<MatSnackBar>('MatSnackBar', ['open']);

    TestBed.configureTestingModule({
      imports: [OrderList],
      providers: [
        provideNoopAnimations(),
        provideRouter([]),
        { provide: AuthService, useValue: authStub },
        { provide: OrderService, useValue: orderService },
      ],
    })
      .overrideProvider(MatDialog, { useValue: dialog })
      .overrideProvider(MatSnackBar, { useValue: snackBar });
  });

  it('loads client orders on init once user is available', () => {
    mount('CLIENT');

    expect(component.role()).toBe('CLIENT');
    expect(component.isSeller()).toBe(false);
    expect(orderService.getMyOrders).toHaveBeenCalledTimes(1);
    expect(component.orders().length).toBe(1);
    expect(component.isLoading()).toBe(false);
    expect(component.hasLoaded()).toBe(true);
  });

  it('loads seller orders when role is SELLER', () => {
    mount('SELLER');

    expect(component.isSeller()).toBe(true);
    expect(orderService.getSellerOrders).toHaveBeenCalledTimes(1);
    expect(orderService.getMyOrders).not.toHaveBeenCalled();
    expect(component.orders()[0].id).toBe('ord-seller');
  });

  it('debounces filter changes before querying', fakeAsync(() => {
    mount('CLIENT');
    orderService.getMyOrders.calls.reset();

    component.filterForm.patchValue({ search: '  book  ', status: 'PENDING' });
    tick(200);
    expect(orderService.getMyOrders).not.toHaveBeenCalled();

    tick(150);
    expect(component.filtersActive()).toBeTrue();
    expect(orderService.getMyOrders).toHaveBeenCalledWith(
      jasmine.objectContaining({ search: 'book', status: 'PENDING', page: 0 }),
    );
  }));

  it('blocks loading and sets dateError when start date is after end date', () => {
    mount('CLIENT');
    orderService.getMyOrders.calls.reset();

    component.filterForm.patchValue({ from: '2026-11-25', to: '2026-11-10' });
    component.applyFilters();

    expect(component.dateError()).toBe('The start date must be on or before the end date.');
    expect(orderService.getMyOrders).not.toHaveBeenCalled();
  });

  it('resets filters and reloads cleanly on clearFilters', () => {
    mount('CLIENT');
    component.filterForm.patchValue({ search: 'mug', from: '2026-11-25', to: '2026-11-10' });
    component.applyFilters();
    orderService.getMyOrders.calls.reset();

    component.clearFilters();

    expect(component.filterForm.getRawValue()).toEqual({
      search: '',
      status: '',
      from: '',
      to: '',
    });
    expect(component.filtersActive()).toBeFalse();
    expect(component.dateError()).toBeNull();
    expect(orderService.getMyOrders).toHaveBeenCalledTimes(1);
  });

  it('cancels an order after confirmation and refreshes the list', async () => {
    mount('CLIENT');
    orderService.getMyOrders.calls.reset();
    const order = sampleOrder({ id: 'ord-42' });

    await component.cancelOrder(order);

    expect(dialog.open).toHaveBeenCalled();
    expect(orderService.cancelOrder).toHaveBeenCalledWith('ord-42');
    expect(snackBar.open).toHaveBeenCalledWith('Order cancelled.', 'Dismiss', { duration: 3000 });
    expect(component.busyOrderId()).toBeNull();
    expect(orderService.getMyOrders).toHaveBeenCalledTimes(1);
  });

  it('does not call cancelOrder when confirmation is dismissed', async () => {
    dialog.open.and.returnValue({ afterClosed: () => of(false) } as never);
    mount('CLIENT');

    await component.cancelOrder(sampleOrder({ id: 'ord-42' }));

    expect(orderService.cancelOrder).not.toHaveBeenCalled();
  });

  it('steps back a page when removing the last order on a later page', async () => {
    mount('CLIENT');
    component.orders.set([sampleOrder({ id: 'ord-last' })]);
    component.pageIndex.set(2);
    orderService.getMyOrders.calls.reset();

    await component.removeOrder(sampleOrder({ id: 'ord-last' }));

    expect(orderService.removeOrder).toHaveBeenCalledWith('ord-last');
    expect(orderService.getMyOrders).toHaveBeenCalledWith(
      jasmine.objectContaining({ page: 1 }),
    );
  });

  it('reorders immediately without a dialog', () => {
    mount('CLIENT');

    component.redoOrder(sampleOrder({ id: 'ord-9' }));

    expect(dialog.open).not.toHaveBeenCalled();
    expect(orderService.redoOrder).toHaveBeenCalledWith('ord-9');
    expect(snackBar.open).toHaveBeenCalledWith(
      'Order placed again.',
      'Dismiss',
      { duration: 3000 },
    );
  });

  it('surfaces an error message and snackbar when loading fails', () => {
    orderService.getMyOrders.and.returnValue(
      throwError(() => ({ error: { message: 'Unable to fetch orders' } })),
    );

    mount('CLIENT');

    expect(component.errorMessage()).toBe('Unable to fetch orders');
    expect(component.isLoading()).toBeFalse();
    expect(snackBar.open).toHaveBeenCalledWith(
      'Unable to fetch orders',
      'Dismiss',
      { duration: 5000 },
    );
  });

  it('keeps busyOrderId cleared and notifies on mutation failure', () => {
    orderService.redoOrder.and.returnValue(throwError(() => new Error('boom')));
    mount('CLIENT');

    component.redoOrder(sampleOrder({ id: 'ord-fail' }));
    expect(el()).toBeTruthy();

    expect(component.busyOrderId()).toBeNull();
    expect(snackBar.open).toHaveBeenCalledWith(
      'boom',
      'Dismiss',
      { duration: 5000 },
    );
  });
});