import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User, Role, AuthResponse } from '../models/user.model';
import { ApiResponse } from '../models/api.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private _accessToken = signal<string | null>(null);
  private _currentUser = signal<User | null>(null);

  readonly currentUser = this._currentUser.asReadonly();
  readonly isAuthenticated = computed(() => !!this._currentUser());
  readonly userRole = computed(() => this._currentUser()?.role ?? null);

  constructor(private http: HttpClient, private router: Router) {
    // On service init, try to restore session from a stored user snapshot
    // (we store only minimal user info in sessionStorage, NOT the token)
    const stored = sessionStorage.getItem('pp_user');
    if (stored) {
      try { this._currentUser.set(JSON.parse(stored)); } catch { }
    }
  }

  login(email: string, password: string): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(
      `${environment.apiUrl}/api/auth/login`,
      { email, password },
      { withCredentials: true }
    ).pipe(tap(res => this.handleAuthResponse(res.data)));
  }

  register(
    email: string,
    password: string,
    fullName: string,
    role: Role
  ): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(
      `${environment.apiUrl}/api/auth/register`,
      { email, password, fullName, role },
      { withCredentials: true }
    ).pipe(tap(res => this.handleAuthResponse(res.data)));
  }

  refresh(): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(
      `${environment.apiUrl}/api/auth/refresh`,
      {},
      { withCredentials: true }
    ).pipe(tap(res => this.handleAuthResponse(res.data)));
  }

  logout(): void {
    this.http.post(
      `${environment.apiUrl}/api/auth/logout`,
      {},
      { withCredentials: true }
    ).subscribe();
    this.clearSession();
    this.router.navigate(['/auth/login']);
  }

  getAccessToken(): string | null { return this._accessToken(); }

  private handleAuthResponse(data: AuthResponse): void {
    this._accessToken.set(data.accessToken);
    const user: User = {
      userId: data.userId,
      email: '',
      fullName: data.fullName,
      role: data.role
    };
    this._currentUser.set(user);
    sessionStorage.setItem('pp_user', JSON.stringify(user));
  }

  private clearSession(): void {
    this._accessToken.set(null);
    this._currentUser.set(null);
    sessionStorage.removeItem('pp_user');
  }
}
