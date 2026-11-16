import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MediaResponse } from '../models/api-response.model';

@Injectable({
  providedIn: 'root'
})
export class MediaService {
  readonly http = inject(HttpClient);

  private parentPath = `/media`;

  uploadImages(formData: FormData): Observable<MediaResponse[]> {
    return this.http.post<MediaResponse[]>(this.parentPath, formData);
  }
  uploadImagesWithProgress(formData: FormData): Observable<HttpEvent<MediaResponse[]>> {
    return this.http.post<MediaResponse[]>(this.parentPath, formData, {
      reportProgress: true,
      observe: 'events',
    });
  }
  deleteImage(id: string): Observable<any> {
    return this.http.delete(`${this.parentPath}/${id}`);
  }
}
