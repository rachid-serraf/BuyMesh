import { HttpEvent, HttpEventType } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { from, of, throwError } from 'rxjs';
import { AuthService } from '../../core/auth/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { MediaService } from '../../core/services/media.service';
import { User } from '../../core/models/user.model';
import { env } from '../../../environments/environment';
import { DeleteAccountDialogComponent } from './delete-account-dialog.component';
import { ProfileComponent } from './user-profile';

class FakeFileReader {
  onload: ((event: { target: FakeFileReader }) => void) | null = null;
  result: string | null = null;

  readAsDataURL(): void {
    this.result = 'data:image/png;base64,avatar';
    this.onload?.({ target: this });
  }
}

describe('ProfileComponent', () => {
  let fixture: ComponentFixture<ProfileComponent>;
  let component: ProfileComponent;
  let auth: {
    user: User | null;
    isSeller: boolean;
    updateCurrentUser: jasmine.Spy;
    logout: jasmine.Spy;
  };
  let userService: jasmine.SpyObj<UserService>;
  let mediaService: jasmine.SpyObj<MediaService>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;
  let dialog: jasmine.SpyObj<MatDialog>;
  let originalFileReader: typeof FileReader;

  const user: User = {
    id: 'user-1',
    name: 'Ada Lovelace',
    email: 'ada@example.com',
    role: 'CLIENT',
  };

  const create = (currentUser: User | null = user): ProfileComponent => {
    auth.user = currentUser;
    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    return component;
  };

  const fileEvent = (files: File[] | null): Event =>
    ({ target: { files } }) as unknown as Event;

  beforeEach(() => {
    originalFileReader = window.FileReader;

    auth = {
      user: null,
      isSeller: false,
      updateCurrentUser: jasmine.createSpy('updateCurrentUser'),
      logout: jasmine.createSpy('logout'),
    };
    userService = jasmine.createSpyObj<UserService>('UserService', [
      'updateUser',
      'updateUserWithProgress',
      'deleteUser',
    ]);
    mediaService = jasmine.createSpyObj<MediaService>('MediaService', [
      'uploadImages',
      'uploadImagesWithProgress',
      'deleteImage',
    ]);
    snackBar = jasmine.createSpyObj<MatSnackBar>('MatSnackBar', ['open']);
    dialog = jasmine.createSpyObj<MatDialog>('MatDialog', ['open']);
    spyOn(console, 'error');

    TestBed.configureTestingModule({
      imports: [NoopAnimationsModule, MatSnackBarModule, MatDialogModule],
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: UserService, useValue: userService },
        { provide: MediaService, useValue: mediaService },
        { provide: Router, useValue: { navigate: jasmine.createSpy('navigate') } },
      ],
    });

    TestBed.overrideComponent(ProfileComponent, {
      set: {
        providers: [
          { provide: MatSnackBar, useValue: snackBar },
          { provide: MatDialog, useValue: dialog },
        ],
      },
    });
  });

  afterEach(() => {
    (window as any).FileReader = originalFileReader;
  });

  it('patches the form and avatar from the current user', () => {
    create({ ...user, avatarUrl: '/avatar.png' });

    expect(component.profileForm.getRawValue()).toEqual({
      name: 'Ada Lovelace',
      email: 'ada@example.com',
    });
    expect(component.avatarPreview()).toBe(`${env.avatarUrl}/avatar.png`);
  });

  it('leaves the avatar empty when the user has none', () => {
    create();

    expect(component.avatarPreview()).toBeNull();
  });

  it('handles a missing user', () => {
    create(null);

    expect(component.currentUser()).toBeNull();
    expect(component.profileForm.getRawValue()).toEqual({ name: '', email: '' });
    expect(component.roleLabel).toBe('CLIENT');
    expect(component.initials).toBe('');
  });

  it('reports role and initials', () => {
    create({ ...user, name: 'Ada', role: 'SELLER' });

    expect(component.roleLabel).toBe('SELLER');
    expect(component.isSeller()).toBeFalse();
    expect(component.initials).toBe('A');
  });

  it('detects name, email, and avatar changes', () => {
    create();
    expect(component.hasChanges).toBeFalse();

    component.profileForm.patchValue({ name: 'Grace Hopper' });
    expect(component.hasChanges).toBeTrue();

    component.profileForm.patchValue({ name: 'Ada Lovelace', email: 'new@example.com' });
    expect(component.hasChanges).toBeTrue();

    component.profileForm.patchValue({ email: 'ada@example.com' });
    expect(component.hasChanges).toBeFalse();

    component.newAvatarUploaded.set(true);
    expect(component.hasChanges).toBeTrue();
  });

  it('requires a valid form before reporting changes', () => {
    create();

    component.profileForm.patchValue({ name: 'A' });

    expect(component.profileForm.invalid).toBeTrue();
    expect(component.hasChanges).toBeFalse();
  });

  it('uploads an image and returns its id', () => {
    create();
    mediaService.uploadImages.and.returnValue(
      of([{ imagesId: 'image-1', status: 'PENDING' }]),
    );
    const callback = jasmine.createSpy('callback');

    (component as any).uploadImage(new File(['x'], 'x.png'), callback);

    expect(mediaService.uploadImages).toHaveBeenCalled();
    expect(callback).toHaveBeenCalledWith('image-1');
  });

  it('does not call back when the upload returns no id', () => {
    create();
    mediaService.uploadImages.and.returnValue(of([]));
    const callback = jasmine.createSpy('callback');

    (component as any).uploadImage(new File(['x'], 'x.png'), callback);

    expect(callback).not.toHaveBeenCalled();
  });

  it('logs upload failures', () => {
    create();
    mediaService.uploadImages.and.returnValue(throwError(() => new Error('upload failed')));

    (component as any).uploadImage(new File(['x'], 'x.png'), jasmine.createSpy('callback'));

    expect(console.error).toHaveBeenCalled();
  });

  it('does nothing when saving without changes', () => {
    create();

    component.saveProfile();

    expect(userService.updateUserWithProgress).not.toHaveBeenCalled();
  });

  it('saves the profile and reports upload progress', () => {
    create();
    const updated: User = { ...user, name: 'Grace Hopper', avatarUrl: '/new.png' };
    userService.updateUserWithProgress.and.returnValue(
      from([
        { type: HttpEventType.UploadProgress, loaded: 50, total: 100 },
        { type: HttpEventType.Response, body: updated },
      ] as HttpEvent<User>[]),
    );
    component.profileForm.patchValue({ name: 'Grace Hopper' });
    component.selectedAvatar.set(new File(['x'], 'x.png'));

    component.saveProfile();

    expect(component.uploadProgress()).toBeNull();
    expect(component.isSaving()).toBeFalse();
    expect(component.profileForm.pristine).toBeTrue();
    expect(component.selectedAvatar()).toBeNull();
    expect(component.newAvatarUploaded()).toBeFalse();
    expect(component.avatarPreview()).toBe(`${env.avatarUrl}/new.png`);
    expect(auth.updateCurrentUser).toHaveBeenCalledWith(updated);
    expect(snackBar.open).toHaveBeenCalledWith('Profile updated successfully!', '✓', {
      duration: 3000,
    });

    const body = userService.updateUserWithProgress.calls.mostRecent().args[0] as FormData;
    expect(body.get('name')).toBe('Grace Hopper');
    expect(body.get('email')).toBe('ada@example.com');
    expect(body.get('avatar')).toBeTruthy();
  });

  it('ignores progress events without a total and unrelated events', () => {
    create();
    userService.updateUserWithProgress.and.returnValue(
      from([
        { type: HttpEventType.UploadProgress, loaded: 10 },
        { type: HttpEventType.Sent },
        { type: HttpEventType.Response, body: null },
      ] as HttpEvent<User>[]),
    );
    component.profileForm.patchValue({ name: 'Grace Hopper' });

    component.saveProfile();

    expect(component.uploadProgress()).toBeNull();
    expect(component.isSaving()).toBeFalse();
    expect(auth.updateCurrentUser).not.toHaveBeenCalled();
  });

  it('reports profile update failures', () => {
    create();
    userService.updateUserWithProgress.and.returnValue(
      throwError(() => ({ error: { message: 'nope' } })),
    );
    component.profileForm.patchValue({ name: 'Grace Hopper' });

    component.saveProfile();

    expect(console.error).toHaveBeenCalled();
    expect(component.uploadProgress()).toBeNull();
    expect(component.isSaving()).toBeFalse();
    expect(snackBar.open).toHaveBeenCalledWith(
      'Failed to update profile. Please try again.',
      'Dismiss',
      { duration: 3000 },
    );
  });

  it('rejects an invalid avatar type', () => {
    create();
    const file = new File(['x'], 'x.txt', { type: 'text/plain' });

    component.onAvatarSelected(fileEvent([file]));

    expect(component.avatarError()).toContain('Invalid file type');
    expect(component.selectedAvatar()).toBeNull();
  });

  it('rejects an avatar that is too large', () => {
    create();
    const file = new File([new Uint8Array(3 * 1024 * 1024)], 'x.png', { type: 'image/png' });

    component.onAvatarSelected(fileEvent([file]));

    expect(component.avatarError()).toContain('File size exceeds');
    expect(component.selectedAvatar()).toBeNull();
  });

  it('accepts a valid avatar and previews it', () => {
    create();
    (window as any).FileReader = FakeFileReader;
    const file = new File(['x'], 'x.png', { type: 'image/png' });

    component.onAvatarSelected(fileEvent([file]));

    expect(component.avatarError()).toBeNull();
    expect(component.selectedAvatar()).toBe(file);
    expect(component.newAvatarUploaded()).toBeTrue();
    expect(component.avatarPreview()).toBe('data:image/png;base64,avatar');
  });

  it('ignores an empty avatar selection', () => {
    create();

    component.onAvatarSelected(fileEvent(null));
    component.onAvatarSelected(fileEvent([]));

    expect(component.selectedAvatar()).toBeNull();
    expect(component.avatarError()).toBeNull();
  });

  it('removes the avatar and falls back to the stored one', () => {
    create({ ...user, avatarUrl: '/avatar.png' });
    component.selectedAvatar.set(new File(['x'], 'x.png'));
    component.newAvatarUploaded.set(false);

    component.removeAvatar();

    expect(component.selectedAvatar()).toBeNull();
    expect(component.newAvatarUploaded()).toBeTrue();
    expect(component.avatarPreview()).toBe('/avatar.png');
  });

  it('clears the preview when removing an avatar without a stored one', () => {
    create();

    component.removeAvatar();

    expect(component.avatarPreview()).toBeNull();
  });

  it('deletes the account after confirmation', () => {
    create();
    dialog.open.and.returnValue({ afterClosed: () => of('delete') } as any);
    spyOn(component, 'deleteAccount');

    component.confirmDelete();

    expect(dialog.open).toHaveBeenCalledWith(DeleteAccountDialogComponent, {
      width: '400px',
      disableClose: true,
    });
    expect(component.deleteAccount).toHaveBeenCalled();
  });

  it('does not delete the account when confirmation is cancelled', () => {
    create();
    dialog.open.and.returnValue({ afterClosed: () => of('cancel') } as any);
    spyOn(component, 'deleteAccount');

    component.confirmDelete();

    expect(component.deleteAccount).not.toHaveBeenCalled();
  });

  it('deletes the account and logs out', () => {
    create();
    userService.deleteUser.and.returnValue(of(void 0));

    component.deleteAccount();

    expect(component.isDeleting()).toBeTrue();
    expect(snackBar.open).toHaveBeenCalledWith('Account deleted successfully', '✓', {
      duration: 3000,
    });
    expect(auth.logout).toHaveBeenCalled();
  });

  it('reports account deletion failures', () => {
    create();
    userService.deleteUser.and.returnValue(throwError(() => new Error('nope')));

    component.deleteAccount();

    expect(console.error).toHaveBeenCalled();
    expect(component.isDeleting()).toBeFalse();
    expect(snackBar.open).toHaveBeenCalledWith(
      'Failed to delete account. Please try again.',
      'Dismiss',
      { duration: 3000 },
    );
  });
});