import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  /**
   * Intercept HTTP requests to add authentication token
   * and handle authentication errors
   */
  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {

    // Add token to headers if it exists
    const token = this.authService.getToken();

    if (token) {

      request = this.addTokenToRequest(request, token);
    }

    // Handle the request and catch errors
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {

        return this.handleError(error);
      })
    );
  }

  /**
   * Add JWT token to request headers
   */
  private addTokenToRequest(request: HttpRequest<unknown>, token: string): HttpRequest<unknown> {

    return request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  /**
   * Handle HTTP errors, especially 401 Unauthorized
   */
  private handleError(error: HttpErrorResponse): Observable<never> {

    if (error.status === 401) {
      
      // Token expired or invalid - logout and redirect to login
      this.authService.logout();
      this.router.navigate(['/login']);
    }

    // Return error as throwError observable
    return throwError(() => error);
  }
}
