import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = '/api/auth';
  private currentUserSubject = new BehaviorSubject<any>(null);

  constructor(private http: HttpClient) {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');
    if (token) this.currentUserSubject.next({ token, role });
  }

  public get currentUserValue() { return this.currentUserSubject.value; }

  login(email: string, password: string):Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, { email, password }).pipe(
      tap(res => {
        localStorage.setItem('token', res.token);
        localStorage.setItem('role', res.role);
        localStorage.setItem('id', res.id); // Save profile ID
        this.currentUserSubject.next(res);
      }),
      catchError(err => throwError(() => err))
    );
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('id');
    this.currentUserSubject.next(null);
  }

  register(data: any) {
    return this.http.post(`${this.apiUrl}/register`, data);
  }

  hasRole(expectedRole: string): boolean {
    const user = this.currentUserValue;
    return user && user.role === expectedRole;
  }
}
