import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private apiUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) {}

  getDoctors(status?: string): Observable<any[]> {
    let url = `${this.apiUrl}/doctors`;
    if (status) url += `?status=${status}`;
    return this.http.get<any[]>(url);
  }

  updateDoctorStatus(doctorId: number, status: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/doctors/${doctorId}/status`, { status });
  }
}
