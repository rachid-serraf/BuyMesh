import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideCharts, withDefaultRegisterables } from 'ng2-charts';
import { NEVER, of, throwError } from 'rxjs';

import { SellerStats } from '../../../core/models/stats.model';
import { StatsService } from '../../../core/services/stats.service';
import { SellerDashboard } from './seller-dashboard';

describe('SellerDashboard', () => {
  let fixture: ComponentFixture<SellerDashboard>;
  let component: SellerDashboard;

  let statsService: jasmine.SpyObj<StatsService>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;

  const emptyStats = (): SellerStats => ({
    totalRevenue: 0,
    ordersCount: 0,
    unitsSold: 0,
    bestSellingProducts: [],
    revenueByMonth: [],
  });

  const sampleStats = (overrides: Partial<SellerStats> = {}): SellerStats => ({
    totalRevenue: 1024.75,
    ordersCount: 12,
    unitsSold: 48,
    bestSellingProducts: [
      {
        productId: 'p1',
        name: 'A very long product name',
        unitsSold: 20,
        revenue: 600,
        imageUrl: 'img-1.jpg',
      },
      { productId: 'p2', name: 'Mug', unitsSold: 8, revenue: 80, imageUrl: null },
    ],
    revenueByMonth: [
      { month: '2026-09', revenue: 300, unitsSold: 10 },
      { month: '2026-10', revenue: 724.75, unitsSold: 38 },
    ],
    ...overrides,
  });

  const el = () => fixture.nativeElement as HTMLElement;

  function mount(stats?: SellerStats | null): void {
    if (stats === null) {
      statsService.getSellerStats.and.returnValue(NEVER);
    } else if (stats) {
      statsService.getSellerStats.and.returnValue(of(stats));
    }
    fixture = TestBed.createComponent(SellerDashboard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  beforeEach(() => {
    statsService = jasmine.createSpyObj<StatsService>('StatsService', ['getSellerStats']);
    statsService.getSellerStats.and.returnValue(of(sampleStats()));
    snackBar = jasmine.createSpyObj<MatSnackBar>('MatSnackBar', ['open']);

    TestBed.configureTestingModule({
      imports: [SellerDashboard],
      providers: [
        provideNoopAnimations(),
        provideRouter([]),
        provideCharts(withDefaultRegisterables()),
        { provide: StatsService, useValue: statsService },
      ],
    }).overrideProvider(MatSnackBar, { useValue: snackBar });
  });

  it('loads seller stats and shows the summary cards', () => {
    mount();

    expect(statsService.getSellerStats).toHaveBeenCalledTimes(1);
    expect(component.isLoading()).toBeFalse();
    expect(component.errorMessage()).toBeNull();
    expect(component.hasData()).toBeTrue();
    expect(component.totalRevenue()).toContain('1,024.75');
    expect(component.unitsSold()).toBe('48');
    expect(component.ordersCount()).toBe('12');

    const cards = el().querySelectorAll('app-stat-card');
    expect(cards.length).toBe(3);
    expect(cards[0].textContent).toContain('Total revenue');
    expect(cards[1].textContent).toContain('Units sold');
    expect(cards[2].textContent).toContain('Orders');
    expect(el().querySelector('.charts-grid')).not.toBeNull();
    expect(el().querySelector('.analytics-note')?.textContent).toContain(
      'Totals are based on orders marked as delivered.',
    );
  });

  it('shows the loading skeletons before stats arrive', () => {
    statsService.getSellerStats.and.returnValue(NEVER);
    fixture = TestBed.createComponent(SellerDashboard);
    component = fixture.componentInstance;

    expect(component.isLoading()).toBeTrue();
    fixture.detectChanges();

    expect(el().querySelector('.charts-grid')?.getAttribute('aria-busy')).toBe('true');
    expect(el().querySelector('app-stat-card')?.textContent).toContain('Loading');
  });

  it('shows the empty state when the store has no data yet', () => {
    mount(emptyStats());

    expect(component.hasData()).toBeFalse();
    expect(component.totalRevenue()).toContain('0.00');
    expect(component.unitsSold()).toBe('0');
    expect(component.ordersCount()).toBe('0');

    const empty = el().querySelector('.empty-state');
    expect(empty).not.toBeNull();
    expect(empty?.textContent).toContain('No data yet');
    expect(el().querySelector('.charts-grid')).toBeNull();
    expect(el().querySelector('.analytics-note')).toBeNull();
  });

  it('treats a lone revenue figure as data', () => {
    mount(emptyStats());
    component.stats.set({ ...emptyStats(), totalRevenue: 10 });

    expect(component.hasData()).toBeTrue();
  });

  it('treats lone units sold as data', () => {
    mount(emptyStats());
    component.stats.set({ ...emptyStats(), unitsSold: 1 });

    expect(component.hasData()).toBeTrue();
  });

  it('treats a lone orders count as data', () => {
    mount(emptyStats());
    component.stats.set({ ...emptyStats(), ordersCount: 1 });

    expect(component.hasData()).toBeTrue();
  });

  it('treats lone best-selling products as data', () => {
    mount(emptyStats());
    component.stats.set({
      ...emptyStats(),
      bestSellingProducts: [{ productId: 'p1', name: 'Mug', unitsSold: 1, revenue: 10 }],
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

  it('builds the best-selling chart data with truncated labels', () => {
    mount();

    const data = component.bestSellingData();
    expect(data.labels).toEqual(['A very ...', 'Mug']);
    expect(data.datasets[0].label).toBe('Units sold');
    expect(data.datasets[0].data).toEqual([20, 8]);
    expect(data.datasets[0].maxBarThickness).toBe(46);
  });

  it('falls back to empty chart data without stats', () => {
    mount(null);

    expect(component.bestSellingData().labels).toEqual([]);
    expect(component.bestSellingData().datasets[0].data).toEqual([]);
    expect(component.revenueByMonthData().labels).toEqual([]);
    expect(component.revenueByMonthData().datasets[0].data).toEqual([]);
  });

  it('builds the revenue line chart with formatted month labels', () => {
    mount();

    const data = component.revenueByMonthData();
    expect(data.labels?.length).toBe(2);
    expect(String(data.labels?.[0])).toContain('2026');
    expect(data.datasets[0].label).toBe('Revenue');
    expect(data.datasets[0].data).toEqual([300, 724.75]);
    expect(data.datasets[0].tension).toBe(0.35);
    expect(data.datasets[0].fill).toBeTrue();
  });

  it('formats money values', () => {
    mount();

    expect(component.formatMoney(9.5)).toContain('9.50');
    expect(component.formatMoney(null)).toContain('0.00');
    expect(component.formatMoney(undefined)).toContain('0.00');
    expect(component.formatMoney(Number.NaN)).toContain('NaN');
  });

  it('formats compact money for axis ticks', () => {
    mount();

    expect(component.formatCompactMoney(1500)).toContain('1.5');
    expect(component.formatCompactMoney(0)).toContain('0');
    expect(component.formatCompactMoney(Number.POSITIVE_INFINITY)).toBe('$0');
    expect(component.formatCompactMoney(Number.NaN)).toBe('$0');
  });

  it('renders the sr-only chart summaries', () => {
    mount();

    const lists = el().querySelectorAll('ul.sr-only');
    expect(lists.length).toBe(2);
    expect(lists[0].textContent).toContain('A very long product name: 20 units sold');
    expect(lists[1].textContent).toContain('revenue');
  });

  it('surfaces the API error message and offers a retry', () => {
    statsService.getSellerStats.and.returnValue(
      throwError(() => ({ error: { message: 'Seller stats are down' } })),
    );

    mount();

    expect(component.errorMessage()).toBe('Seller stats are down');
    expect(component.isLoading()).toBeFalse();
    expect(snackBar.open).toHaveBeenCalledWith('Seller stats are down', 'Dismiss', {
      duration: 5000,
    });

    const alert = el().querySelector('.analytics-alert');
    expect(alert?.textContent).toContain('Could not load your analytics');
    expect(alert?.textContent).toContain('Seller stats are down');
    expect(el().querySelector('.empty-state')).toBeNull();

    statsService.getSellerStats.and.returnValue(of(sampleStats()));
    el().querySelector<HTMLButtonElement>('.analytics-alert button')?.click();
    fixture.detectChanges();

    expect(component.errorMessage()).toBeNull();
    expect(statsService.getSellerStats).toHaveBeenCalledTimes(2);
  });

  it('falls back when the error body has no usable message', () => {
    statsService.getSellerStats.and.returnValue(throwError(() => new Error('plain')));
    mount();

    expect(component.errorMessage()).toBe('Could not load your analytics. Please try again.');
    expect(snackBar.open).toHaveBeenCalledWith(
      'Could not load your analytics. Please try again.',
      'Dismiss',
      { duration: 5000 },
    );
  });

  it('ignores non-string error messages from the API', () => {
    statsService.getSellerStats.and.returnValue(throwError(() => ({ error: { message: 42 } })));
    mount();

    expect(component.errorMessage()).toBe('Could not load your analytics. Please try again.');
  });

  it('labels bar chart tooltips with unit counts', () => {
    mount();

    const label = component.bestSellingOptions.plugins?.tooltip?.callbacks?.label as (
      context: unknown,
    ) => string;
    expect(label({ parsed: { y: 20 } })).toBe('20 units sold');
    expect(label({ parsed: { y: null } })).toBe('0 units sold');
  });

  it('labels line chart tooltips with revenue', () => {
    mount();

    const label = component.revenueByMonthOptions.plugins?.tooltip?.callbacks?.label as (
      context: unknown,
    ) => string;
    expect(label({ parsed: { y: 1500 } })).toBe(`Revenue: ${component.formatMoney(1500)}`);
    expect(label({ parsed: {} })).toBe(`Revenue: ${component.formatMoney(0)}`);
    expect(label({ parsed: { y: null } })).toBe(`Revenue: ${component.formatMoney(0)}`);
  });

  it('compacts axis tick values', () => {
    mount();

    const callback = component.revenueByMonthOptions.scales?.['y']?.ticks?.callback as (
      value: number,
      index: number,
      ticks: unknown[],
    ) => string;
    expect(callback(2500, 0, [])).toBe(component.formatCompactMoney(2500));
  });

  it('keeps the chart options stable and responsive', () => {
    mount();

    const bestSellingY = component.bestSellingOptions.scales?.['y'] as
      { beginAtZero?: boolean } | undefined;
    const revenueY = component.revenueByMonthOptions.scales?.['y'] as
      { beginAtZero?: boolean } | undefined;

    expect(component.bestSellingOptions.responsive).toBeTrue();
    expect(component.bestSellingOptions.maintainAspectRatio).toBeFalse();
    expect(bestSellingY?.beginAtZero).toBeTrue();
    expect(component.revenueByMonthOptions.interaction).toEqual({
      mode: 'index',
      intersect: false,
    });
    expect(revenueY?.beginAtZero).toBeTrue();
  });
});
