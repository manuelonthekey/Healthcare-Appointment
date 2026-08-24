import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class DoctorService {
  private apiUrl = 'http://localhost:8080/api/doctors';

  constructor(private http: HttpClient) {}

  addAvailability(profileId: number, data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/${profileId}/availability`, data);
  }

  addLeave(profileId: number, date: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${profileId}/leaves`, { leaveDate: date });
  }

  getAvailableSlots(profileId: number, date: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/${profileId}/slots?date=${date}`);
  }
}
