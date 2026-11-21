import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NEVER, of, throwError } from 'rxjs';

import { BuyerStats } from '../../../core/models/stats.model';
import { StatsService } from '../../../core/services/stats.service';
import { provideCharts, withDefaultRegisterables } from 'ng2-charts';
import { env } from '../../../../environments/environment';
import { ProfileDashboard } from './profile-dashboard';

describe('ProfileDashboard', () => {
  let fixture: ComponentFixture<ProfileDashboard>;
  let component: ProfileDashboard;

  let statsService: jasmine.SpyObj<StatsService>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;

  const emptyStats = (): BuyerStats => ({
    totalSpent: 0,
    ordersCount: 0,
    mostBoughtProducts: [],
    bestProducts: [],
    topCategories: [],
  });

  const sampleStats = (overrides: Partial<BuyerStats> = {}): BuyerStats => ({
    totalSpent: 240.5,
    ordersCount: 4,
    mostBoughtProducts: [
      {
        productId: 'p1',
        name: 'A very long product name',
        quantity: 6,
        amountSpent: 120,
        imageUrl: 'img-1.jpg',
      },
      { productId: 'p2', name: 'Mug', quantity: 2, amountSpent: 30, imageUrl: null },
    ],
    bestProducts: [
      { productId: 'p1', name: 'Desk lamp', quantity: 2, amountSpent: 90, imageUrl: 'lamp.jpg' },
    ],
    topCategories: [
      { category: 'HOME_LIVING', quantity: 5, amountSpent: 100 },
      { category: 'BOOKS', quantity: 1, amountSpent: 20 },
    ],
    ...overrides,
  });

  const el = () => fixture.nativeElement as HTMLElement;

  function mount(stats?: BuyerStats | null): void {
    if (stats === null) {
      statsService.getBuyerStats.and.returnValue(NEVER);
    } else if (stats) {
      statsService.getBuyerStats.and.returnValue(of(stats));
    }
    fixture = TestBed.createComponent(ProfileDashboard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  beforeEach(() => {
    statsService = jasmine.createSpyObj<StatsService>('StatsService', ['getBuyerStats']);
    statsService.getBuyerStats.and.returnValue(of(sampleStats()));
    snackBar = jasmine.createSpyObj<MatSnackBar>('MatSnackBar', ['open']);

    TestBed.configureTestingModule({
      imports: [ProfileDashboard],
      providers: [
        provideNoopAnimations(),
        provideRouter([]),
        provideCharts(withDefaultRegisterables()),
        { provide: StatsService, useValue: statsService },
      ],
    }).overrideProvider(MatSnackBar, { useValue: snackBar });
  });

  it('loads buyer stats and shows the summary cards', () => {
    mount();

    expect(statsService.getBuyerStats).toHaveBeenCalledTimes(1);
    expect(component.isLoading()).toBeFalse();
    expect(component.errorMessage()).toBeNull();
    expect(component.hasData()).toBeTrue();
    expect(component.totalSpent()).toContain('240.50');
    expect(component.ordersCount()).toBe('4');

    const cards = el().querySelectorAll('app-stat-card');
    expect(cards.length).toBe(2);
    expect(cards[0].textContent).toContain('Total spent');
    expect(cards[1].textContent).toContain('Orders');
    expect(el().querySelector('.charts-grid')).not.toBeNull();
    expect(el().querySelector('.empty-state')).toBeNull();
  });

  it('shows the loading skeletons before stats arrive', () => {
    statsService.getBuyerStats.and.returnValue(NEVER);
    fixture = TestBed.createComponent(ProfileDashboard);
    component = fixture.componentInstance;

    expect(component.isLoading()).toBeTrue();
    fixture.detectChanges();

    expect(el().querySelector('.charts-grid')?.getAttribute('aria-busy')).toBe('true');
    expect(el().querySelector('app-stat-card')?.textContent).toContain('Loading');
  });

  it('shows the empty state when there is no data yet', () => {
    mount(emptyStats());

    expect(component.hasData()).toBeFalse();
    expect(component.totalSpent()).toContain('0.00');
    expect(component.ordersCount()).toBe('0');

    const empty = el().querySelector('.empty-state');
    expect(empty).not.toBeNull();
    expect(empty?.textContent).toContain('No data yet');
    expect(el().querySelector('.charts-grid')).toBeNull();
    expect(el().querySelector('.best-products')).toBeNull();
  });

  it('treats a lone orders count as data', () => {
    mount(emptyStats());
    component.stats.set({ ...emptyStats(), ordersCount: 1 });

    expect(component.hasData()).toBeTrue();
  });

  it('treats lone most-bought products as data', () => {
    mount(emptyStats());
    component.stats.set({
      ...emptyStats(),
      mostBoughtProducts: [{ productId: 'p1', name: 'Mug', quantity: 1, amountSpent: 10 }],
    });

    expect(component.hasData()).toBeTrue();
  });

  it('treats lone best products as data', () => {
    mount(emptyStats());
    component.stats.set({
      ...emptyStats(),
      bestProducts: [{ productId: 'p1', name: 'Mug', quantity: 1, amountSpent: 10 }],
    });

    expect(component.hasData()).toBeTrue();
  });

  it('treats lone top categories as data', () => {
    mount(emptyStats());
    component.stats.set({
      ...emptyStats(),
      topCategories: [{ category: 'BOOKS', quantity: 1, amountSpent: 10 }],
    });

    expect(component.hasData()).toBeTrue();
  });

  it('stays empty when stats are missing entirely', () => {
    mount(null);

    expect(component.stats()).toBeNull();
    expect(component.hasData()).toBeFalse();
    expect(component.isLoading()).toBeTrue();
    expect(el().querySelector('.empty-state')).toBeNull();
  });

  it('builds the most-bought chart data with truncated labels', () => {
    mount();

    const data = component.mostBoughtData();
    expect(data.labels).toEqual(['A very ...', 'Mug']);
    expect(data.datasets[0].label).toBe('Units bought');
    expect(data.datasets[0].data).toEqual([6, 2]);
    expect(data.datasets[0].hoverBackgroundColor).toBe('#00439c');
  });

  it('falls back to empty chart data without stats', () => {
    mount(null);

    expect(component.mostBoughtData().labels).toEqual([]);
    expect(component.mostBoughtData().datasets[0].data).toEqual([]);
    expect(component.topCategoriesData().labels).toEqual([]);
    expect(component.topCategoriesData().datasets[0].data).toEqual([]);
  });

  it('builds the categories chart with formatted labels', () => {
    mount();

    const data = component.topCategoriesData();
    expect(data.labels).toEqual(['Home Living', 'Books']);
    expect(data.datasets[0].data).toEqual([100, 20]);
    expect(data.datasets[0].borderWidth).toBe(2);
    expect(data.datasets[0].hoverOffset).toBe(6);
  });

  it('formats money and category names', () => {
    mount();

    expect(component.formatMoney(12.5)).toContain('12.50');
    expect(component.formatMoney(null)).toContain('0.00');
    expect(component.formatMoney(undefined)).toContain('0.00');
    expect(component.formatCategoryName('HOME_LIVING')).toBe('Home Living');
    expect(component.formatCategoryName('')).toBe('');
    expect(component.formatCategoryName('books')).toBe('Books');
  });

  it('resolves image sources for products', () => {
    mount();

    expect(component.imageSrc(null)).toBeNull();
    expect(component.imageSrc('lamp.jpg')).toBe(`${env.mediaUrl}/lamp.jpg`);
    expect(component.imageSrc('https://cdn.example/x.png')).toBe('https://cdn.example/x.png');
    expect(component.imageSrc('/uploads/x.png')).toBe('/uploads/x.png');
    expect(component.imageSrc('data:image/png;base64,aa')).toBe('data:image/png;base64,aa');
    expect(component.imageSrc('blob:https://app/1')).toBe('blob:https://app/1');
    expect(component.imageSrc('http://cdn.example/x.png')).toBe('http://cdn.example/x.png');
  });

  it('renders the best products list and falls back without one', () => {
    mount();

    const rows = el().querySelectorAll('.product-row');
    expect(rows.length).toBe(1);
    expect(rows[0].textContent).toContain('Desk lamp');
    expect(rows[0].querySelector('img')?.getAttribute('src')).toBe(`${env.mediaUrl}/lamp.jpg`);

    component.stats.set({ ...sampleStats(), bestProducts: [] });
    fixture.detectChanges();

    expect(el().querySelector('.no-products')?.textContent).toContain('No product data yet.');
    expect(el().querySelectorAll('.product-row').length).toBe(0);
  });

  it('shows the loading icon when a best product has no image', () => {
    mount();
    component.stats.set({
      ...sampleStats(),
      bestProducts: [
        { productId: 'p9', name: 'No image', quantity: 1, amountSpent: 5, imageUrl: null },
      ],
    });
    fixture.detectChanges();

    expect(el().querySelector('.product-thumb img')).toBeNull();
    expect(el().querySelector('.product-thumb lucide-angular')).not.toBeNull();
  });

  it('renders the sr-only chart summaries', () => {
    mount();

    const lists = el().querySelectorAll('ul.sr-only');
    expect(lists.length).toBe(2);
    expect(lists[0].textContent).toContain('A very long product name: 6 units');
    expect(lists[1].textContent).toContain('Home Living: 5 units');
  });

  it('surfaces the API error message and offers a retry', () => {
    statsService.getBuyerStats.and.returnValue(
      throwError(() => ({ error: { message: 'Stats are down' } })),
    );

    mount();

    expect(component.errorMessage()).toBe('Stats are down');
    expect(component.isLoading()).toBeFalse();
    expect(snackBar.open).toHaveBeenCalledWith('Stats are down', 'Dismiss', {
      duration: 5000,
    });

    const alert = el().querySelector('.analytics-alert');
    expect(alert?.textContent).toContain('Could not load your analytics');
    expect(alert?.textContent).toContain('Stats are down');
    expect(el().querySelector('.empty-state')).toBeNull();

    statsService.getBuyerStats.and.returnValue(of(sampleStats()));
    el().querySelector<HTMLButtonElement>('.analytics-alert button')?.click();
    fixture.detectChanges();

    expect(component.errorMessage()).toBeNull();
    expect(statsService.getBuyerStats).toHaveBeenCalledTimes(2);
  });

  it('falls back when the error body has no usable message', () => {
    statsService.getBuyerStats.and.returnValue(throwError(() => new Error('plain')));
    mount();

    expect(component.errorMessage()).toBe('Could not load your analytics. Please try again.');
    expect(snackBar.open).toHaveBeenCalledWith(
      'Could not load your analytics. Please try again.',
      'Dismiss',
      { duration: 5000 },
    );
  });

  it('ignores blank error messages from the API', () => {
    statsService.getBuyerStats.and.returnValue(throwError(() => ({ error: { message: '   ' } })));
    mount();

    expect(component.errorMessage()).toBe('Could not load your analytics. Please try again.');
  });

  it('labels bar chart tooltips with unit counts', () => {
    mount();

    const label = component.mostBoughtOptions.plugins?.tooltip?.callbacks?.label as (
      context: unknown,
    ) => string;
    expect(label({ parsed: { x: 6 } })).toBe('6 units');
    expect(label({ parsed: { x: null } })).toBe('0 units');
  });

  it('labels doughnut tooltips with category shares', () => {
    mount();

    const label = component.topCategoriesOptions.plugins?.tooltip?.callbacks?.label as (
      context: unknown,
    ) => string;
    expect(label({ label: 'Home Living', parsed: 12.5 })).toBe(
      `Home Living: ${component.formatMoney(12.5)}`,
    );
  });

  it('keeps the chart options stable and responsive', () => {
    mount();

    expect(component.mostBoughtOptions.indexAxis).toBe('y');
    expect(component.mostBoughtOptions.responsive).toBeTrue();
    expect(component.mostBoughtOptions.maintainAspectRatio).toBeFalse();
    expect(component.topCategoriesOptions.cutout).toBe('62%');
    expect(component.topCategoriesOptions.plugins?.legend?.position).toBe('bottom');
    expect(component.mostBoughtOptions.scales?.['y']?.grid?.display).toBeFalse();
  });
});
