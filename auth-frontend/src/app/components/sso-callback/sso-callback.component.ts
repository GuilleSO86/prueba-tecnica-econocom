import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService, LoginResponse } from '../../services/auth.service';

@Component({
  selector: 'app-sso-callback',
  templateUrl: './sso-callback.component.html',
  styleUrls: ['./sso-callback.component.scss']
})
export class SsoCallbackComponent implements OnInit {
  isLoading: boolean = true;
  errorMessage: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {

    this.handleSsoCallback();
  }

  /**
   * Handle the SSO callback
   * Extracts the authorization code and state from URL parameters
   * and exchanges them for tokens
   */
  private handleSsoCallback(): void {

    // Extract query parameters from URL
    const code = this.route.snapshot.queryParamMap.get('code');
    const state = this.route.snapshot.queryParamMap.get('state');

    if (!code) {

      this.errorMessage = 'No se recibió código de autorización del proveedor SSO.';

      this.isLoading = false;

      this.showErrorAndRedirect();

      return;
    }

    // Exchange authorization code for tokens
    this.http.get<LoginResponse>(`/api/auth/sso/callback?code=${code}&state=${state || ''}`)
      .subscribe({

        next: (response) => {

          this.authService.setToken(response.token);
          this.authService.setRefreshToken(response.refreshToken);

          this.showSuccessAndRedirect();
        },

        error: (error) => {

          this.isLoading = false;

          if (error.status === 401) {

            this.errorMessage = 'Autenticación SSO fallida. Código de autorización inválido.';

          } else if (error.status === 0) {

            this.errorMessage = 'No se pudo conectar con el servidor.';

          } else {
            this.errorMessage = 'Error en el proceso de autenticación SSO.';
          }

          this.showErrorAndRedirect();
        }
      });
  }

  /**
   * Show success message and redirect to home
   */
  private showSuccessAndRedirect(): void {

    this.snackBar.open('Inicio de sesión SSO exitoso', 'Cerrar', {
      duration: 3000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: ['success-snackbar']
    });

    this.router.navigate(['/home']);
  }

  /**
   * Show error message and redirect to login
   */
  private showErrorAndRedirect(): void {

    this.snackBar.open(this.errorMessage, 'Cerrar', {
      duration: 5000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: ['error-snackbar']
    });
    
    this.router.navigate(['/login']);
  }
}
