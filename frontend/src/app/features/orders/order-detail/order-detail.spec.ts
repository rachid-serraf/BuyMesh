import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NEVER, of, throwError } from 'rxjs';

import { AuthService } from '../../../core/auth/services/auth.service';
import { Order, OrderStatus } from '../../../core/models/order.model';
import { Role, User } from '../../../core/models/user.model';
import { OrderService } from '../../../core/services/order.service';
import { env } from '../../../../environments/environment';
import { ConfirmOrderActionData } from '../dialogs/confirm-order-action/confirm-order-action';
import { OrderDetail } from './order-detail';

describe('OrderDetail', () => {
  let fixture: ComponentFixture<OrderDetail>;
  let component: OrderDetail;

  let authStub: { user: User | null };
  let orderService: jasmine.SpyObj<OrderService>;
  let dialog: jasmine.SpyObj<MatDialog>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;

  const sampleOrder = (overrides: Partial<Order> = {}): Order =>
    ({
      id: 'ord-42',
      status: 'PENDING' as OrderStatus,
      createdAt: '2026-11-01T10:00:00Z',
      total: 100,
      items: [
        { productId: 'p1', name: 'Desk lamp', price: 45, quantity: 2, imageUrl: 'img-1.jpg' },
        { productId: 'p2', name: 'Notebook', price: 5, quantity: 1 },
      ],
      address: {
        fullName: 'Ada Lovelace',
        phone: '+1 555 0100',
        line1: '1 Main St',
        city: 'Springfield',
        postalCode: '62701',
        country: 'USA',
      },
      ...overrides,
    }) as Order;

  const el = () => fixture.nativeElement as HTMLElement;

  function setRole(role: Role): void {
    authStub.user = { id: 'user-1', role } as User;
  }

  function mount(role: Role = 'CLIENT', order?: Order | null): void {
    setRole(role);
    if (order) {
      orderService.getOrder.and.returnValue(of(order));
    }
    fixture = TestBed.createComponent(OrderDetail);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('id', 'ord-42');
    fixture.detectChanges();
  }

  beforeEach(() => {
    authStub = { user: { id: 'user-1', role: 'CLIENT' } as User };

    orderService = jasmine.createSpyObj<OrderService>('OrderService', [
      'getOrder',
      'updateStatus',
      'cancelOrder',
      'redoOrder',
    ]);
    orderService.getOrder.and.returnValue(of(sampleOrder()));
    orderService.updateStatus.and.returnValue(of(void 0));
    orderService.cancelOrder.and.returnValue(of(void 0));
    orderService.redoOrder.and.returnValue(of(void 0));

    dialog = jasmine.createSpyObj<MatDialog>('MatDialog', ['open']);
    dialog.open.and.returnValue({ afterClosed: () => of(true) } as never);

    snackBar = jasmine.createSpyObj<MatSnackBar>('MatSnackBar', ['open']);

    TestBed.configureTestingModule({
      imports: [OrderDetail],
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

  it('loads the order and computes summary values', () => {
    mount('CLIENT');

    expect(orderService.getOrder).toHaveBeenCalledWith('ord-42');
    expect(component.isLoading()).toBeFalse();
    expect(component.errorMessage()).toBeNull();
    expect(component.order()?.id).toBe('ord-42');

    expect(component.itemsCount()).toBe(3);
    expect(component.itemsSubtotal()).toBe(95);
    expect(component.deliveryFee()).toBe(5);
    expect(component.subtitle()).toBe('The order has been placed and is waiting for the seller.');
    expect(component.addressLines()).toEqual([
      'Ada Lovelace',
      '+1 555 0100',
      '1 Main St',
      '62701 Springfield',
      'USA',
    ]);

    expect(el().querySelector('.order-id')?.textContent).toContain('#ord-42');
    expect(el().querySelector('.summary-rows')?.textContent).toContain('$95.00');
    expect(el().querySelector('.summary-rows')?.textContent).toContain('$5.00');
    expect(el().querySelector('.summary-rows')?.textContent).toContain('$100.00');
    expect(el().querySelector('address')?.textContent).toContain('Springfield');
    expect(el().querySelector('.payment-method')?.textContent).toContain('Pay on Delivery');
  });

  it('shows the loading skeleton before the order arrives', () => {
    orderService.getOrder.and.returnValue(NEVER);
    setRole('CLIENT');
    fixture = TestBed.createComponent(OrderDetail);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('id', 'ord-42');

    expect(component.isLoading()).toBeTrue();
    fixture.detectChanges();

    expect(el().querySelector('.order-skeleton')).not.toBeNull();
    expect(el().querySelector('.order-header')).toBeNull();
  });

  it('shows the empty state when the order cannot be loaded', () => {
    orderService.getOrder.and.returnValue(
      throwError(() => ({ error: { message: 'Order not found' } })),
    );

    mount('CLIENT');

    expect(component.order()).toBeNull();
    expect(component.isLoading()).toBeFalse();
    expect(component.errorMessage()).toBe('Order not found');
    expect(snackBar.open).toHaveBeenCalledWith('Order not found', 'Dismiss', { duration: 5000 });

    const empty = el().querySelector('.empty-state');
    expect(empty).not.toBeNull();
    expect(empty?.textContent).toContain("We couldn't load this order");
    expect(empty?.textContent).toContain('Order not found');
  });

  it('falls back to a generic message when the load error has no body', () => {
    orderService.getOrder.and.returnValue(throwError(() => ({})));

    mount('CLIENT');

    expect(component.errorMessage()).toBe('Could not load this order.');
    expect(el().querySelector('.empty-state')?.textContent).toContain('Could not load this order.');
  });

  it('retries the load from the empty state', () => {
    orderService.getOrder.and.returnValue(throwError(() => ({ error: 'boom' })));
    mount('CLIENT');
    expect(component.order()).toBeNull();

    orderService.getOrder.and.returnValue(of(sampleOrder()));
    el().querySelector<HTMLButtonElement>('.error-actions button')?.click();
    fixture.detectChanges();

    expect(orderService.getOrder).toHaveBeenCalledTimes(2);
    expect(component.order()?.id).toBe('ord-42');
    expect(component.errorMessage()).toBeNull();
  });

  it('keeps the loaded order and shows a refresh alert when a reload fails', () => {
    mount('CLIENT');
    orderService.getOrder.and.returnValue(
      throwError(() => ({ error: { message: 'Refresh failed' } })),
    );

    component.loadOrder();
    fixture.detectChanges();

    expect(component.order()?.id).toBe('ord-42');
    expect(component.errorMessage()).toBe('Refresh failed');

    const alert = el().querySelector('.order-alert');
    expect(alert).not.toBeNull();
    expect(alert?.textContent).toContain('Could not refresh this order');
    expect(alert?.textContent).toContain('Refresh failed');
    expect(el().querySelector('.order-header')).not.toBeNull();
  });

  it('retries from the refresh alert', () => {
    mount('CLIENT');
    orderService.getOrder.and.returnValue(throwError(() => ({ error: 'temporarily down' })));
    component.loadOrder();
    fixture.detectChanges();
    orderService.getOrder.and.returnValue(of(sampleOrder({ id: 'ord-42' })));

    el().querySelector<HTMLButtonElement>('.order-alert button')?.click();
    fixture.detectChanges();

    expect(component.errorMessage()).toBeNull();
    expect(component.order()?.id).toBe('ord-42');
    expect(el().querySelector('.order-alert')).toBeNull();
  });

  it('offers no actions to a client with a shipped order', () => {
    mount('CLIENT', sampleOrder({ status: 'SHIPPED' }));

    expect(component.hasActions()).toBeFalse();
    expect(component.forward()).toBeNull();
    expect(component.canCancel()).toBeFalse();
    expect(component.canRedo()).toBeFalse();
    expect(el().querySelector('.actions-card')).toBeNull();
  });

  it('lets a client cancel a pending order after confirmation', async () => {
    mount('CLIENT', sampleOrder({ status: 'PENDING' }));

    expect(component.canCancel()).toBeTrue();
    expect(component.canRedo()).toBeFalse();
    expect(component.hasActions()).toBeTrue();

    await component.cancel();

    expect(dialog.open).toHaveBeenCalled();
    const config = dialog.open.calls.mostRecent().args[1] as {
      width?: string;
      data?: ConfirmOrderActionData;
    };
    expect(config.width).toBe('400px');
    expect(config.data).toEqual(
      jasmine.objectContaining({
        title: 'Cancel this order?',
        confirmLabel: 'Cancel order',
        cancelLabel: 'Keep order',
        destructive: true,
      }),
    );
    expect(orderService.cancelOrder).toHaveBeenCalledWith('ord-42');
    expect(snackBar.open).toHaveBeenCalledWith('Order cancelled.', 'Dismiss', {
      duration: 3000,
    });
    expect(component.isBusy()).toBeFalse();
  });

  it('does not cancel when the confirmation dialog is dismissed', async () => {
    dialog.open.and.returnValue({ afterClosed: () => of(false) } as never);
    mount('CLIENT');

    await component.cancel();

    expect(dialog.open).toHaveBeenCalled();
    expect(orderService.cancelOrder).not.toHaveBeenCalled();
    expect(snackBar.open).not.toHaveBeenCalled();
  });

  it('does nothing when cancelling without a loaded order', async () => {
    orderService.getOrder.and.returnValue(throwError(() => new Error('missing')));
    mount('CLIENT');

    await component.cancel();

    expect(dialog.open).not.toHaveBeenCalled();
    expect(orderService.cancelOrder).not.toHaveBeenCalled();
  });

  it('lets a client redo a cancelled order without a dialog', () => {
    mount('CLIENT', sampleOrder({ status: 'CANCELLED' }));

    expect(component.canRedo()).toBeTrue();
    expect(component.canCancel()).toBeFalse();
    expect(component.hasActions()).toBeTrue();

    component.redo();

    expect(dialog.open).not.toHaveBeenCalled();
    expect(orderService.redoOrder).toHaveBeenCalledWith('ord-42');
    expect(snackBar.open).toHaveBeenCalledWith('Order placed again.', 'Dismiss', {
      duration: 3000,
    });
  });

  it('does nothing when redoing without a loaded order', () => {
    orderService.getOrder.and.returnValue(throwError(() => new Error('missing')));
    mount('CLIENT');

    component.redo();

    expect(orderService.redoOrder).not.toHaveBeenCalled();
  });

  it('advances a seller through the fulfilment flow', () => {
    mount('SELLER', sampleOrder({ status: 'PENDING' }));

    expect(component.role()).toBe('SELLER');
    expect(component.forward()).toEqual({ status: 'CONFIRMED', label: 'Confirm order' });
    expect(component.hasActions()).toBeTrue();

    component.advance();

    expect(orderService.updateStatus).toHaveBeenCalledWith('ord-42', 'CONFIRMED');
    expect(snackBar.open).toHaveBeenCalledWith('Order marked as confirmed.', 'Dismiss', {
      duration: 3000,
    });
    expect(component.isBusy()).toBeFalse();
  });

  it('does not advance when there is no transition', () => {
    mount('SELLER', sampleOrder({ status: 'DELIVERED' }));

    expect(component.forward()).toBeNull();
    expect(component.hasActions()).toBeFalse();

    component.advance();

    expect(orderService.updateStatus).not.toHaveBeenCalled();
  });

  it('does not advance when the order is missing', () => {
    orderService.getOrder.and.returnValue(throwError(() => new Error('missing')));
    mount('SELLER');

    component.advance();

    expect(orderService.updateStatus).not.toHaveBeenCalled();
  });

  it('reports a failed status update', () => {
    orderService.updateStatus.and.returnValue(
      throwError(() => ({ error: { message: 'Status is invalid' } })),
    );
    mount('SELLER');

    component.advance();

    expect(snackBar.open).toHaveBeenCalledWith('Status is invalid', 'Dismiss', { duration: 5000 });
    expect(component.isBusy()).toBeFalse();
  });

  it('reports a failed cancel with the fallback message', async () => {
    orderService.cancelOrder.and.returnValue(throwError(() => new Error('nope')));
    mount('CLIENT');

    await component.cancel();

    expect(snackBar.open).toHaveBeenCalledWith('nope', 'Dismiss', { duration: 5000 });
    expect(component.isBusy()).toBeFalse();
  });

  it('marks a delivered order free when the total matches the items', () => {
    mount(
      'CLIENT',
      sampleOrder({
        status: 'DELIVERED',
        total: 95,
        address: '1 Main St\nSpringfield, IL',
      }),
    );

    expect(component.deliveryFee()).toBe(0);
    expect(component.canRedo()).toBeTrue();
    expect(el().querySelector('.summary-rows')?.textContent).toContain('Free');
    expect(el().querySelector('address')?.textContent).toContain('1 Main St');
    expect(el().querySelector('address')?.textContent).toContain('Springfield, IL');
  });

  it('shows the plural item label and no address block content', () => {
    mount(
      'CLIENT',
      sampleOrder({
        items: [{ productId: 'p1', name: 'Only', price: 10, quantity: 1 }],
        total: 10,
        address: null,
      }),
    );

    expect(component.itemsCount()).toBe(1);
    expect(component.deliveryFee()).toBe(0);
    expect(component.addressLines()).toEqual([]);
    expect(el().querySelector('.page-subtitle')?.textContent).toContain('1 item');
    expect(el().querySelector('address')).toBeNull();
    expect(el().querySelector('.info-card .muted')?.textContent).toContain(
      'No delivery address provided.',
    );
  });

  it('resolves image sources for every url shape', () => {
    mount('CLIENT');

    expect(component.imageSrc(null)).toBeNull();
    expect(component.imageSrc(undefined)).toBeNull();
    expect(component.imageSrc('')).toBeNull();
    expect(component.imageSrc('https://cdn.example/x.png')).toBe('https://cdn.example/x.png');
    expect(component.imageSrc('http://cdn.example/x.png')).toBe('http://cdn.example/x.png');
    expect(component.imageSrc('data:image/png;base64,aa')).toBe('data:image/png;base64,aa');
    expect(component.imageSrc('blob:https://app/123')).toBe('blob:https://app/123');
    expect(component.imageSrc('/uploads/x.png')).toBe('/uploads/x.png');
    expect(component.imageSrc('img-1.jpg')).toBe(`${env.mediaUrl}/img-1.jpg`);
  });

  it('renders item images from the media base and placeholders without one', () => {
    mount(
      'CLIENT',
      sampleOrder({
        items: [
          { productId: 'p1', name: 'Desk lamp', price: 45, quantity: 1, imageUrl: 'img-1.jpg' },
          { productId: 'p2', name: 'Notebook', price: 5, quantity: 1, imageUrl: null },
        ],
        total: 50,
      }),
    );

    const images = el().querySelectorAll('.item-media img');
    expect(images.length).toBe(1);
    expect(images[0].getAttribute('src')).toBe(`${env.mediaUrl}/img-1.jpg`);
    expect(el().querySelectorAll('.item-media lucide-angular').length).toBe(1);
  });

  it('shows the empty items row when the order has no lines', () => {
    mount('CLIENT', sampleOrder({ items: [], total: 0 }));

    expect(el().querySelector('.order-item-empty')?.textContent).toContain(
      'No items on this order.',
    );
    expect(component.itemsSubtotal()).toBe(0);
    expect(component.itemsCount()).toBe(0);
  });

  it('shows the breadcrumb order id only after the order loads', () => {
    orderService.getOrder.and.returnValue(throwError(() => new Error('later')));
    mount('CLIENT');
    expect(el().querySelector('.breadcrumb .current')?.textContent).toContain('Order details');

    orderService.getOrder.and.returnValue(of(sampleOrder()));
    component.loadOrder();
    fixture.detectChanges();

    expect(el().querySelector('.breadcrumb .current')?.textContent).toContain('#ord-42');
  });

  it('zeroes the summary computeds while no order is loaded', () => {
    orderService.getOrder.and.returnValue(NEVER);
    mount('CLIENT');

    expect(component.order()).toBeNull();
    expect(component.itemsSubtotal()).toBe(0);
    expect(component.deliveryFee()).toBe(0);
    expect(component.itemsCount()).toBe(0);
    expect(component.subtitle()).toBe('');
    expect(component.addressLines()).toEqual([]);
    expect(component.hasActions()).toBeFalse();
  });

  it('defaults the role to CLIENT when nobody is signed in', () => {
    authStub.user = null;
    orderService.getOrder.and.returnValue(of(sampleOrder({ status: 'SHIPPED' })));
    fixture = TestBed.createComponent(OrderDetail);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('id', 'ord-42');
    fixture.detectChanges();

    expect(component.role()).toBe('CLIENT');
    expect(component.hasActions()).toBeFalse();
  });
});
