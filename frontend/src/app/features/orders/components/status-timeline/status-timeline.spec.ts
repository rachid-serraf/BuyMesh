import { ComponentFixture, TestBed } from '@angular/core/testing';
import { OrderStatus } from '../../../../core/models/order.model';
import { ORDER_FLOW } from '../../utils/order.utils';
import { StatusTimeline } from './status-timeline';

describe('StatusTimeline', () => {
  let fixture: ComponentFixture<StatusTimeline>;
  let component: StatusTimeline;

  const el = () => fixture.nativeElement as HTMLElement;

  function mount(status: OrderStatus): void {
    TestBed.configureTestingModule({ imports: [StatusTimeline] });
    fixture = TestBed.createComponent(StatusTimeline);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('status', status);
    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('exposes the fulfilment steps', () => {
    mount('PENDING');

    expect(component.steps).toEqual(ORDER_FLOW);
    expect(component.steps.map((step) => step.status)).toEqual([
      'PENDING',
      'CONFIRMED',
      'SHIPPED',
      'DELIVERED',
    ]);
  });

  it('marks the first step as current for a pending order', () => {
    mount('PENDING');

    expect(component.currentIndex()).toBe(0);
    expect(component.isCancelled()).toBeFalse();
    expect(component.currentDescription()).toBe(
      'The order has been placed and is waiting for the seller.',
    );

    const steps = el().querySelectorAll('.timeline-step');
    expect(steps.length).toBe(4);
    expect(steps[0].classList).toContain('current');
    expect(steps[0].classList).toContain('complete');
    expect(steps[0].getAttribute('aria-current')).toBe('step');
    expect(steps[1].classList).not.toContain('current');
    expect(steps[1].classList).not.toContain('complete');
    expect(el().querySelector('.timeline-caption')?.textContent).toContain(
      'waiting for the seller',
    );
  });

  it('marks every step complete for a delivered order', () => {
    mount('DELIVERED');

    expect(component.currentIndex()).toBe(3);
    expect(component.currentDescription()).toBe('The order was delivered successfully.');

    const steps = el().querySelectorAll('.timeline-step');
    steps.forEach((step, i) => {
      expect(step.classList).toContain('complete');
      if (i < 3) {
        expect(step.classList).toContain('filled');
      } else {
        expect(step.classList).not.toContain('filled');
      }
    });
    expect(steps[3].classList).toContain('current');
    expect(steps[2].classList).not.toContain('current');
  });

  it('locates intermediate statuses in the flow', () => {
    mount('CONFIRMED');
    expect(component.currentIndex()).toBe(1);
    expect(component.currentDescription()).toBe(
      'The seller confirmed the order and is preparing it.',
    );

    fixture.componentRef.setInput('status', 'SHIPPED');
    fixture.detectChanges();

    expect(component.currentIndex()).toBe(2);
    expect(component.currentDescription()).toBe('The order has left the seller and is on its way.');
  });

  it('shows the cancelled state outside the fulfilment flow', () => {
    mount('CANCELLED');

    expect(component.isCancelled()).toBeTrue();
    expect(component.cancelledLabel()).toBe('Cancelled');
    expect(component.currentIndex()).toBe(-1);
    expect(component.currentDescription()).toBe('');

    expect(el().querySelector('.timeline-cancelled')).not.toBeNull();
    expect(el().querySelector('.timeline-cancelled')?.textContent).toContain('Cancelled');
    expect(el().querySelector('.timeline')).toBeNull();
  });
});
