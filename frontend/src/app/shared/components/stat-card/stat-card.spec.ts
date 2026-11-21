import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Wallet } from 'lucide-angular';
import { StatCard } from './stat-card';

describe('StatCard', () => {
  let fixture: ComponentFixture<StatCard>;
  let component: StatCard;

  const el = () => fixture.nativeElement as HTMLElement;

  function mount(): void {
    TestBed.configureTestingModule({ imports: [StatCard] });
    fixture = TestBed.createComponent(StatCard);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('label', 'Total spent');
    fixture.componentRef.setInput('value', '$120.00');
    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('renders the label and value', () => {
    mount();

    expect(el().querySelector('.stat-label')?.textContent).toContain('Total spent');
    expect(el().querySelector('.stat-value')?.textContent).toContain('$120.00');
    expect(el().querySelector('.stat-hint')).toBeNull();
    expect(el().querySelector('lucide-angular')).toBeNull();
    expect(el().querySelector('article')?.classList).not.toContain('is-loading');
  });

  it('uses the accent tone by default', () => {
    mount();

    const icon = el().querySelector('.stat-icon');
    expect(icon?.classList).toContain('tone-accent');
    expect(icon?.classList).not.toContain('tone-success');
    expect(icon?.classList).not.toContain('tone-neutral');
  });

  it('applies the requested tone', () => {
    mount();
    fixture.componentRef.setInput('tone', 'success');
    fixture.detectChanges();

    const icon = el().querySelector('.stat-icon');
    expect(icon?.classList).toContain('tone-success');
    expect(icon?.classList).not.toContain('tone-accent');
  });

  it('applies the neutral tone', () => {
    mount();
    fixture.componentRef.setInput('tone', 'neutral');
    fixture.detectChanges();

    expect(el().querySelector('.stat-icon')?.classList).toContain('tone-neutral');
  });

  it('shows the hint when one is given', () => {
    mount();
    fixture.componentRef.setInput('hint', 'Delivered orders only');
    fixture.detectChanges();

    expect(el().querySelector('.stat-hint')?.textContent).toContain('Delivered orders only');
  });

  it('renders the icon when provided', () => {
    mount();
    fixture.componentRef.setInput('icon', Wallet);
    fixture.detectChanges();

    expect(el().querySelector('lucide-angular')).not.toBeNull();
  });

  it('shows a skeleton instead of the value while loading', () => {
    mount();
    fixture.componentRef.setInput('loading', true);
    fixture.detectChanges();

    expect(el().querySelector('article')?.classList).toContain('is-loading');
    expect(el().querySelector('.stat-value')).toBeNull();
    expect(el().querySelector('.stat-skeleton')).not.toBeNull();
    expect(el().querySelector('.sr-only')?.textContent).toContain('Loading Total spent');
  });

  it('keeps the value visible when not loading', () => {
    mount();
    fixture.componentRef.setInput('loading', false);
    fixture.detectChanges();

    expect(el().querySelector('.stat-value')?.textContent).toContain('$120.00');
    expect(el().querySelector('.stat-skeleton')).toBeNull();
  });

  it('exposes the default input values', () => {
    mount();

    expect(component.hint()).toBe('');
    expect(component.icon()).toBeNull();
    expect(component.tone()).toBe('accent');
    expect(component.loading()).toBeFalse();
  });
});
