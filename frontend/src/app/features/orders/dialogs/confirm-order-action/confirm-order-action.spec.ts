import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ConfirmOrderActionData, ConfirmOrderActionDialog } from './confirm-order-action';

describe('ConfirmOrderActionDialog', () => {
  let fixture: ComponentFixture<ConfirmOrderActionDialog>;
  let component: ConfirmOrderActionDialog;
  let dialogRef: jasmine.SpyObj<MatDialogRef<ConfirmOrderActionDialog, boolean>>;

  const el = () => fixture.nativeElement as HTMLElement;

  async function create(data: ConfirmOrderActionData): Promise<void> {
    dialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);

    await TestBed.configureTestingModule({
      imports: [ConfirmOrderActionDialog],
      providers: [
        provideNoopAnimations(),
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: MAT_DIALOG_DATA, useValue: data },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ConfirmOrderActionDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('closes with true when the confirm button is pressed', async () => {
    await create({ title: 'Ship it?', message: 'Mark as shipped.' });

    const buttons = el().querySelectorAll('button');
    expect(buttons.length).toBe(2);
    buttons[1].click();

    expect(dialogRef.close).toHaveBeenCalledWith(true);
    expect(dialogRef.close).toHaveBeenCalledTimes(1);
  });

  it('closes with false when the cancel button is pressed', async () => {
    await create({ title: 'Ship it?', message: 'Mark as shipped.' });

    el().querySelectorAll('button')[0].click();

    expect(dialogRef.close).toHaveBeenCalledWith(false);
  });

  it('forwards an explicit confirmation from the component API', async () => {
    await create({ title: 'Ship it?', message: 'Mark as shipped.' });

    component.close(true);
    component.close(false);

    expect(dialogRef.close).toHaveBeenCalledWith(true);
    expect(dialogRef.close).toHaveBeenCalledWith(false);
  });

  it('falls back to the default button labels', async () => {
    await create({ title: 'Cancel this order?', message: 'Cannot be undone.' });

    const buttons = el().querySelectorAll('button');
    expect(buttons[0].textContent).toContain('Keep order');
    expect(buttons[1].textContent).toContain('Confirm');
    expect(buttons[1].classList).toContain('mat-primary');
    expect(el().querySelector('.dialog-icon')?.classList).not.toContain('destructive');
    expect(el().querySelector('h2')?.textContent).toContain('Cancel this order?');
    expect(el().querySelector('mat-dialog-content')?.textContent).toContain('Cannot be undone.');
  });

  it('uses custom labels and the warn colour for destructive actions', async () => {
    await create({
      title: 'Cancel this order?',
      message: 'Order #1 will be cancelled.',
      confirmLabel: 'Cancel order',
      cancelLabel: 'Keep order',
      destructive: true,
    });

    const buttons = el().querySelectorAll('button');
    expect(buttons[0].textContent).toContain('Keep order');
    expect(buttons[1].textContent).toContain('Cancel order');
    expect(buttons[1].classList).toContain('mat-warn');
    expect(el().querySelector('.dialog-icon')?.classList).toContain('destructive');
  });
});
