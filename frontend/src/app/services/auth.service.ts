import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
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
        this.currentUserSubject.next(res);
      }),
      catchError(err => throwError(() => err))
    );
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
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
