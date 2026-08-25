import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private apiUrl = '/api/admin';

  constructor(private http: HttpClient) {}

  getDoctors(status?: string): Observable<any[]> {
    let url = `${this.apiUrl}/doctors`;
    if (status) url += `?status=${status}`;
    return this.http.get<any[]>(url);
  }

  updateDoctorStatus(doctorId: number, status: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/doctors/${doctorId}/status`, { status });
  }

  createDoctor(doctor: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/doctors`, doctor);
  }

  getWorkingHours(doctorId: number): Observable<any[]> {
    return this.http.get<any[]>(`/api/doctors/${doctorId}/availability`);
  }

  setWorkingHours(doctorId: number, availabilities: any[]): Observable<any> {
    return this.http.put(`${this.apiUrl}/doctors/${doctorId}/working-hours`, availabilities);
  }

  getLeaves(doctorId: number): Observable<any[]> {
    return this.http.get<any[]>(`/api/doctors/${doctorId}/leaves`);
  }

  addLeave(doctorId: number, leave: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/doctors/${doctorId}/leaves`, leave);
  }

  deleteLeave(doctorId: number, leaveId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/doctors/${doctorId}/leaves/${leaveId}`);
  }
}
