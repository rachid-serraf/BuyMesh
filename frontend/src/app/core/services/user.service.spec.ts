import { HttpEvent, HttpEventType, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { User } from '../models/user.model';
import { UserService } from './user.service';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  const mockUser: User = {
    id: 'user-1',
    name: 'Ada Lovelace',
    email: 'ada@example.com',
    role: 'CLIENT',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), UserService],
    });

    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('updates the current user via PUT /users/me', () => {
    const formData = new FormData();
    formData.append('name', 'Grace Hopper');
    const updatedUser: User = { ...mockUser, name: 'Grace Hopper' };

    let result: User | undefined;
    service.updateUser(formData).subscribe((res) => {
      result = res;
    });

    const req = httpMock.expectOne('/users/me');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toBe(formData);
    req.flush(updatedUser);

    expect(result).toEqual(updatedUser);
  });

  it('updates the current user and emits upload progress and response events', () => {
    const formData = new FormData();
    formData.append('name', 'Grace Hopper');
    const updatedUser: User = { ...mockUser, name: 'Grace Hopper', avatarUrl: '/avatar.png' };

    const events: HttpEvent<User>[] = [];
    service.updateUserWithProgress(formData).subscribe((event) => {
      events.push(event);
    });

    const req = httpMock.expectOne('/users/me');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toBe(formData);
    expect(req.request.reportProgress).toBeTrue();

    req.event({ type: HttpEventType.UploadProgress, loaded: 50, total: 100 });
    req.flush(updatedUser);

    expect(events.length).toBe(3);
    expect(events[0].type).toBe(HttpEventType.Sent);
    expect(events[1]).toEqual({ type: HttpEventType.UploadProgress, loaded: 50, total: 100 });
    expect(events[2].type).toBe(HttpEventType.Response);
    if (events[2].type === HttpEventType.Response) {
      expect(events[2].body).toEqual(updatedUser);
    }
  });

  it('deletes the current user via DELETE /users/me', () => {
    let completed = false;

    service.deleteUser().subscribe({
      next: () => {
        completed = true;
      },
    });

    const req = httpMock.expectOne('/users/me');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);

    expect(completed).toBeTrue();
  });
});