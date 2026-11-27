import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ProductService } from './product.service';
import { ProductCreateRequest } from '../models/product.model';

describe('ProductService', () => {
  let service: ProductService;
  let httpMock: HttpTestingController;

  const recentDate = () => new Date().toISOString();
  const oldDate = () => new Date(Date.now() - 60 * 60 * 1000).toISOString();

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ProductService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('creates a product', () => {
    const payload = { name: 'Phone' };

    service.createProduct(payload).subscribe((result) => expect(result).toEqual({ id: 'product-1' }));

    const request = httpMock.expectOne('/products');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush({ id: 'product-1' });
  });

  it('gets the categories', () => {
    const categories = ['PHONES', 'LAPTOPS'];

    service.getCategories().subscribe((result) => expect(result).toEqual(categories));

    const request = httpMock.expectOne('/products/categories');
    expect(request.request.method).toBe('GET');
    request.flush(categories);
  });

  it('gets and normalizes a product', () => {
    let product: any;

    service.getProduct('product-1').subscribe((value) => (product = value));

    const request = httpMock.expectOne('/products/product-1');
    expect(request.request.method).toBe('GET');
    request.flush({
      name: 'Phone',
      description: 'A phone',
      price: 100,
      userId: 'seller-1',
      quantity: 5,
      category: 'PHONES',
      imagesIds: ['image-1', JSON.stringify(['image-2', 'image-3']), 42, 'image-4'],
    });

    expect(product.name).toBe('Phone');
    expect(product.description).toBe('A phone');
    expect(product.price).toBe(100);
    expect(product.userId).toBe('seller-1');
    expect(product.quantity).toBe(5);
    expect(product.category).toBe('PHONES');
    expect(product.rating).toBe(4.8);
    expect(product.reviewsCount).toBe(124);
    expect(product.oldPrice).toBeCloseTo(120.4);
    expect(product.thumbnails).toEqual(['image-1', 'image-2', 'image-3', 'image-4']);
    expect(product.mainImage).toBe('image-1');
  });

  it('returns no images when the product has no image ids', () => {
    let product: any;

    service.getProduct('product-2').subscribe((value) => (product = value));
    httpMock.expectOne('/products/product-2').flush({
      name: 'Phone',
      description: 'A phone',
      price: 100,
      userId: 'seller-1',
      quantity: 5,
      category: 'PHONES',
    });

    expect(product.thumbnails).toEqual([]);
    expect(product.mainImage).toBeUndefined();
  });

  it('gets the product list and flags new products', () => {
    let products: any[] | undefined;

    service.getListOfProducts().subscribe((value) => (products = value));

    const request = httpMock.expectOne('/products');
    expect(request.request.method).toBe('GET');
    request.flush([
      { id: 'p1', name: 'New', price: 1, category: 'PHONES', createdAt: recentDate() },
      { id: 'p2', name: 'Old', price: 1, category: 'PHONES', createdAt: oldDate() },
      { id: 'p3', name: 'Missing', price: 1, category: 'PHONES' },
    ]);

    expect(products?.map((product) => product.badge)).toEqual(['New', undefined, undefined]);
  });

  it('gets the product list before a given time', () => {
    service.getListOfProducts('2024-01-01').subscribe();

    const request = httpMock.expectOne('/products?beforeTime=2024-01-01');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('searches products with params and normalizes results', () => {
    let result: any;

    service
      .searchProducts({
        q: 'phone',
        category: '',
        minPrice: undefined,
        maxPrice: 0,
        page: 1,
        size: 10,
      } as any)
      .subscribe((value) => (result = value));

    const request = httpMock.expectOne((req) => req.url === '/products/search');
    expect(request.request.params.get('q')).toBe('phone');
    expect(request.request.params.has('category')).toBeFalse();
    expect(request.request.params.has('minPrice')).toBeFalse();
    expect(request.request.params.get('maxPrice')).toBe('0');
    expect(request.request.params.get('page')).toBe('1');
    expect(request.request.params.get('size')).toBe('10');
    request.flush({
      content: [
        { id: 'p1', name: 'New', price: 1, category: 'PHONES', createdAt: recentDate() },
        { id: 'p2', name: 'Old', price: 1, category: 'PHONES', createdAt: oldDate(), image: 'a.png' },
      ],
      page: 1,
      size: 10,
      totalElements: 2,
      totalPages: 1,
    });

    expect(result.content[0].image).toBe('');
    expect(result.content[0].badge).toBe('New');
    expect(result.content[1].image).toBe('a.png');
    expect(result.content[1].badge).toBeUndefined();
  });

  it('gets my products and flags new ones', () => {
    let products: any[] | undefined;

    service.getMyProductsList().subscribe((value) => (products = value));

    const request = httpMock.expectOne('/products/user');
    expect(request.request.method).toBe('GET');
    request.flush([
      { id: 'p1', name: 'New', price: 1, category: 'PHONES', createdAt: recentDate() },
      { id: 'p2', name: 'Invalid', price: 1, category: 'PHONES', createdAt: 'not-a-date' },
    ]);

    expect(products?.map((product) => product.badge)).toEqual(['New', undefined]);
  });

  it('gets my products before a given time', () => {
    service.getMyProductsList('2024-01-01').subscribe();

    const request = httpMock.expectOne('/products/user?beforeTime=2024-01-01');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('treats an unparsable creation date as not new', () => {
    service = TestBed.inject(ProductService);

    expect((service as any).isNew(Symbol('invalid'))).toBeFalse();
  });

  it('updates a product', () => {
    const changes: ProductCreateRequest = {
      name: 'Phone',
      description: 'A phone',
      price: 100,
      quantity: 5,
      imagesIds: ['image-1'],
      category: 'PHONES',
    };

    service.updateProduct('product-1', changes).subscribe((result) => expect(result).toEqual(changes));

    const request = httpMock.expectOne('/products/product-1');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(changes);
    request.flush(changes);
  });

  it('deletes a product', () => {
    service.deleteProduct('product-1').subscribe();

    const request = httpMock.expectOne('/products/product-1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });
});
