import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  refreshToken: string;
  expiresIn: number;
  type: string;
}

export interface SsoResponse {
  ssoUrl: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = '/api/auth';
  private readonly TOKEN_KEY = 'auth_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Login with credentials
   * @param credentials - User email and password
   * @returns Observable with login response containing tokens
   */
  login(credentials: LoginCredentials): Observable<LoginResponse> {

    return this.http.post<LoginResponse>(`${this.API_URL}/login`, credentials).pipe(
      tap(response => {

        this.storeToken(response.token);
        this.storeRefreshToken(response.refreshToken);
        this.isAuthenticatedSubject.next(true);
      })
    );
  }

  /**
   * Logout and clear stored tokens
   */
  logout(): void {

    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);

    this.isAuthenticatedSubject.next(false);
  }

  /**
   * Check if user is authenticated
   * @returns boolean indicating authentication status
   */
  isAuthenticated(): boolean {

    return this.hasToken();
  }

  /**
   * Get stored JWT token
   * @returns JWT token or null
   */
  getToken(): string | null {

    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Get stored refresh token
   * @returns Refresh token or null
   */
  getRefreshToken(): string | null {

    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  /**
   * Store JWT token in localStorage
   */
  private storeToken(token: string): void {

    localStorage.setItem(this.TOKEN_KEY, token);
  }

  /**
   * Store refresh token in localStorage
   */
  private storeRefreshToken(refreshToken: string): void {

    localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
  }

  /**
   * Set JWT token (public method for SSO login)
   */
  setToken(token: string): void {

    this.storeToken(token);
    this.isAuthenticatedSubject.next(true);
  }

  /**
   * Initiate SSO login flow
   * Calls backend /api/auth/sso which returns 302 redirect with Location header
   * 
   * @returns Observable with the full HTTP response to access Location header
   */
  initiateSso(): Observable<SsoResponse> {

    return this.http.get<SsoResponse>(`${this.API_URL}/sso`);
  }

  /**
   * Set refresh token (public method for SSO login)
   */
  setRefreshToken(refreshToken: string): void {

    this.storeRefreshToken(refreshToken);
  }

  /**
   * Check if token exists in localStorage
   */
  private hasToken(): boolean {
    
    return !!localStorage.getItem(this.TOKEN_KEY);
  }
}
