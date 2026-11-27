import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { OrderService } from './order.service';
import { OrderAddress, OrderPage } from '../models/order.model';

describe('OrderService', () => {
  let service: OrderService;
  let httpMock: HttpTestingController;

  const address: OrderAddress = {
    fullName: 'Ada Lovelace',
    phone: '+1 555 0100',
    line1: '1 Main St',
    city: 'Springfield',
    postalCode: '62701',
    country: 'USA',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(OrderService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('gets my orders with query params and normalizes a paginated response', () => {
    let result: OrderPage | undefined;

    service
      .getMyOrders({ search: 'phone', status: 'PENDING', page: 2, size: 10 })
      .subscribe((value) => (result = value));

    const request = httpMock.expectOne((req) => req.url === '/orders/me');
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('search')).toBe('phone');
    expect(request.request.params.get('status')).toBe('PENDING');
    expect(request.request.params.get('page')).toBe('2');
    expect(request.request.params.get('size')).toBe('10');
    request.flush({
      content: [{ id: 'order-1', status: 'PENDING', items: [], total: 0 }],
      page: 2,
      size: 10,
      totalElements: 21,
      totalPages: 3,
    });

    expect(result?.content.length).toBe(1);
    expect(result?.page).toBe(2);
    expect(result?.size).toBe(10);
    expect(result?.totalElements).toBe(21);
    expect(result?.totalPages).toBe(3);
  });

  it('skips empty query params', () => {
    service
      .getMyOrders({ search: '', status: undefined, from: undefined, to: '', page: 0 } as any)
      .subscribe();

    const request = httpMock.expectOne((req) => req.url === '/orders/me');
    expect(request.request.params.has('search')).toBeFalse();
    expect(request.request.params.has('status')).toBeFalse();
    expect(request.request.params.has('from')).toBeFalse();
    expect(request.request.params.has('to')).toBeFalse();
    expect(request.request.params.get('page')).toBe('0');
    request.flush({ content: [] });
  });

  it('normalizes a bare array response', () => {
    let result: OrderPage | undefined;

    service.getMyOrders().subscribe((value) => (result = value));
    httpMock.expectOne('/orders/me').flush([
      { id: 'order-1', status: 'CONFIRMED', items: [], total: 0 },
      { id: 'order-2', status: 'SHIPPED', items: [], total: 0 },
    ]);

    expect(result?.content.map((order) => order.id)).toEqual(['order-1', 'order-2']);
    expect(result?.page).toBe(0);
    expect(result?.size).toBe(2);
    expect(result?.totalElements).toBe(2);
    expect(result?.totalPages).toBe(1);
  });

  it('returns an empty page for an empty array response', () => {
    let result: OrderPage | undefined;

    service.getMyOrders().subscribe((value) => (result = value));
    httpMock.expectOne('/orders/me').flush([]);

    expect(result).toEqual({
      content: [],
      page: 0,
      size: 0,
      totalElements: 0,
      totalPages: 0,
    });
  });

  it('gets seller orders and derives missing pagination fields', () => {
    let result: OrderPage | undefined;

    service.getSellerOrders({ page: 1 }).subscribe((value) => (result = value));

    const request = httpMock.expectOne((req) => req.url === '/orders/seller');
    expect(request.request.params.get('page')).toBe('1');
    request.flush({
      content: [{ _id: 'order-1', status: 'DELIVERED', items: [], total: 0 }],
      number: 1,
      totalElements: 25,
    });

    expect(result?.page).toBe(1);
    expect(result?.size).toBe(1);
    expect(result?.totalPages).toBe(25);
  });

  it('handles an empty page object', () => {
    let result: OrderPage | undefined;

    service.getSellerOrders().subscribe((value) => (result = value));
    httpMock.expectOne('/orders/seller').flush({});

    expect(result).toEqual({
      content: [],
      page: 0,
      size: 0,
      totalElements: 0,
      totalPages: 0,
    });
  });

  it('derives a single page when the size is zero but content exists', () => {
    let result: OrderPage | undefined;

    service.getSellerOrders().subscribe((value) => (result = value));
    httpMock.expectOne('/orders/seller').flush({
      content: [{ id: 'order-1', status: 'PENDING', items: [] }],
      size: 0,
    });

    expect(result?.size).toBe(0);
    expect(result?.totalElements).toBe(1);
    expect(result?.totalPages).toBe(1);
  });

  it('normalizes a single order, its items, total, and address', () => {
    let order: any;

    service.getOrder('order-1').subscribe((value) => (order = value));

    const request = httpMock.expectOne('/orders/order-1');
    expect(request.request.method).toBe('GET');
    request.flush({
      _id: 'order-1',
      userId: 'buyer-1',
      sellerId: 'seller-1',
      status: 'canceled',
      items: [
        {
          id: 'product-1',
          name: '   ',
          price: '12.50',
          quantity: '2',
          image: 'image.png',
          imagesIds: ['image-1', 'image-2'],
        },
        {
          productId: 'product-2',
          name: 'Phone',
          unitPrice: { $numberDecimal: '5.5' },
          quantity: 0,
          imagesIds: ['image-3'],
        },
        {
          name: 'Tablet',
          price: { $numberDecimal: '3' },
          imagesIds: [123, 'image-4'],
        },
        { name: 'Watch', price: 'not-a-number', quantity: null },
      ],
      totalAmount: { $numberDecimal: '100' },
      address: {
        name: 'Ada Lovelace',
        phone: '+1 555 0100',
        street: '1 Main St',
        addressLine2: 'Apt 4',
        city: 'Springfield',
        state: 'IL',
        zip: '62701',
        country: 'USA',
      },
      created_at: '2024-01-01T00:00:00Z',
    });

    expect(order.id).toBe('order-1');
    expect(order.buyerId).toBe('buyer-1');
    expect(order.sellerId).toBe('seller-1');
    expect(order.status).toBe('CANCELLED');
    expect(order.total).toBe(100);
    expect(order.createdAt).toBe('2024-01-01T00:00:00Z');
    expect(order.address).toEqual({
      fullName: 'Ada Lovelace',
      phone: '+1 555 0100',
      line1: '1 Main St',
      line2: 'Apt 4',
      city: 'Springfield',
      state: 'IL',
      postalCode: '62701',
      country: 'USA',
    });
    expect(order.items).toEqual([
      {
        productId: 'product-1',
        name: 'Product',
        imageUrl: 'image.png',
        price: 12.5,
        quantity: 2,
      },
      {
        productId: 'product-2',
        name: 'Phone',
        imageUrl: 'image-3',
        price: 5.5,
        quantity: 0,
      },
      {
        productId: '',
        name: 'Tablet',
        imageUrl: 'image-4',
        price: 3,
        quantity: 1,
      },
      {
        productId: '',
        name: 'Watch',
        imageUrl: null,
        price: 0,
        quantity: 1,
      },
    ]);
  });

  it('computes the total and normalizes status and string addresses', () => {
    let order: any;

    service.getOrder('order-2').subscribe((value) => (order = value));
    httpMock.expectOne('/orders/order-2').flush({
      id: 'order-2',
      status: 'completed',
      items: [{ productId: 'product-1', price: 10, quantity: 2 }],
      address: '1 Main St',
    });

    expect(order.total).toBe(20);
    expect(order.status).toBe('DELIVERED');
    expect(order.address).toBe('1 Main St');
    expect(order.createdAt).toBeUndefined();
  });

  it('falls back to pending for unknown statuses and null for invalid addresses', () => {
    let order: any;

    service.getOrder('order-3').subscribe((value) => (order = value));
    httpMock.expectOne('/orders/order-3').flush({
      id: 'order-3',
      status: 'unknown',
      items: [],
      address: 42,
    });

    expect(order.status).toBe('PENDING');
    expect(order.total).toBe(0);
    expect(order.address).toBeNull();
  });

  it('treats a missing address as null', () => {
    let order: any;

    service.getOrder('order-4').subscribe((value) => (order = value));
    httpMock.expectOne('/orders/order-4').flush({ id: 'order-4', status: 'CONFIRMED' });

    expect(order.address).toBeNull();
    expect(order.items).toEqual([]);
  });

  it('defaults missing id, status, and non-array items', () => {
    let order: any;

    service.getOrder('order-6').subscribe((value) => (order = value));
    httpMock.expectOne('/orders/order-6').flush({
      items: { not: 'an array' },
      total: { $numberDecimal: 5 },
    });

    expect(order.id).toBe('');
    expect(order.status).toBe('PENDING');
    expect(order.items).toEqual([]);
    expect(order.total).toBe(0);
  });

  it('ignores empty decimal strings', () => {
    let order: any;

    service.getOrder('order-7').subscribe((value) => (order = value));
    httpMock.expectOne('/orders/order-7').flush({
      id: 'order-7',
      status: 'PENDING',
      items: [{ productId: 'product-1', name: 'Phone', price: { $numberDecimal: '   ' }, quantity: 1 }],
    });

    expect(order.items[0].price).toBe(0);
  });

  it('rejects non-finite decimal values', () => {
    let order: any;

    service.getOrder('order-8').subscribe((value) => (order = value));
    httpMock.expectOne('/orders/order-8').flush({
      id: 'order-8',
      status: 'PENDING',
      items: [{ productId: 'product-1', name: 'Phone', price: { $numberDecimal: 'NaN' }, quantity: 1 }],
    });

    expect(order.items[0].price).toBe(0);
  });

  it('normalizes the address object with alternate keys', () => {
    let order: any;

    service.getOrder('order-5').subscribe((value) => (order = value));
    httpMock.expectOne('/orders/order-5').flush({
      id: 'order-5',
      status: 'CONFIRMED',
      items: [],
      address: { addressLine1: '2 Side St', addressLine2: '', zip: '99999' },
    });

    expect(order.address).toEqual({
      fullName: undefined,
      phone: undefined,
      line1: '2 Side St',
      line2: undefined,
      city: undefined,
      state: undefined,
      postalCode: '99999',
      country: undefined,
    });
  });

  it('buys a product now and normalizes the created order', () => {
    let order: any;

    service.buyNow('product-1', 3, address).subscribe((value) => (order = value));

    const request = httpMock.expectOne('/orders/buy');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ productId: 'product-1', quantity: 3, address });
    request.flush({
      id: 'order-1',
      status: 'PENDING',
      items: [{ productId: 'product-1', price: 10, quantity: 3 }],
    });

    expect(order.id).toBe('order-1');
    expect(order.total).toBe(30);
  });

  it('defaults the buy-now quantity to one', () => {
    service.buyNow('product-1', undefined as any, address).subscribe();

    const request = httpMock.expectOne('/orders/buy');
    expect(request.request.body.quantity).toBe(1);
    request.flush({ id: 'order-1', status: 'PENDING', items: [] });
  });

  it('updates the order status', () => {
    service.updateStatus('order-1', 'SHIPPED').subscribe();

    const request = httpMock.expectOne('/orders/order-1/status');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ status: 'SHIPPED' });
    request.flush(null);
  });

  it('cancels, redoes, and removes orders', () => {
    service.cancelOrder('order-1').subscribe();
    const cancel = httpMock.expectOne('/orders/order-1/cancel');
    expect(cancel.request.method).toBe('POST');
    expect(cancel.request.body).toEqual({});
    cancel.flush(null);

    service.redoOrder('order-1').subscribe();
    const redo = httpMock.expectOne('/orders/order-1/redo');
    expect(redo.request.method).toBe('POST');
    expect(redo.request.body).toEqual({});
    redo.flush(null);

    service.removeOrder('order-1').subscribe();
    const remove = httpMock.expectOne('/orders/order-1');
    expect(remove.request.method).toBe('DELETE');
    remove.flush(null);
  });
});
