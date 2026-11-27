import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MediaService } from './media.service';
import { MediaResponse } from '../models/api-response.model';

describe('MediaService', () => {
  let service: MediaService;
  let httpMock: HttpTestingController;

  const media: MediaResponse = { imagesId: 'image-1', status: 'PENDING' };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(MediaService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('uploads images', () => {
    const formData = new FormData();
    formData.append('file', new File(['x'], 'x.png'));
    let result: MediaResponse[] | undefined;

    service.uploadImages(formData).subscribe((value) => (result = value));

    const request = httpMock.expectOne('/media');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toBe(formData);
    request.flush([media]);

    expect(result).toEqual([media]);
  });

  it('uploads images with progress events', () => {
    const formData = new FormData();
    formData.append('file', new File(['x'], 'x.png'));

    service.uploadImagesWithProgress(formData).subscribe();

    const request = httpMock.expectOne('/media');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toBe(formData);
    expect(request.request.reportProgress).toBeTrue();
    request.flush([media]);
  });

  it('deletes an image', () => {
    service.deleteImage('image-1').subscribe();

    const request = httpMock.expectOne('/media/image-1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });
});
