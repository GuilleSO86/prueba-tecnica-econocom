import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpClient } from '@angular/common/http';
import { AuthService, LoginResponse } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  loginForm!: FormGroup;
  hidePassword: boolean = true;
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.initForm();
  }

  /**
   * Initialize the login form with validators
   */
  private initForm(): void {

    this.loginForm = this.fb.group({
      email: [
        '',
        [
          Validators.required,
          Validators.email
        ]
      ],
      password: [
        '',
        [
          Validators.required,
          Validators.minLength(6)
        ]
      ]
    });
  }

  /**
   * Handle form submission
   */
  onSubmit(): void {

    if (this.loginForm.invalid) {

      this.markFormGroupTouched(this.loginForm);

      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const credentials = {
                          email: this.loginForm.get('email')?.value,
                          password: this.loginForm.get('password')?.value
    };

    this.authService.login(credentials).subscribe({

      next: (response) => {

        this.isLoading = false;

        this.showSuccessMessage('Inicio de sesión exitoso');

        this.resetForm();

        this.router.navigate(['/home']);
      },

      error: (error) => {

        this.isLoading = false;

        this.handleLoginError(error);
      }
    });
  }

  /**
   * Initiate SSO login flow (simulated)
   * Calls backend /api/auth/sso which returns the SSO callback URL
   * Then makes HTTP call to that URL to get tokens directly
   */
  initiateSsoLogin(): void {

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.initiateSso().subscribe({
      
      next: (response) => {

        const redirectUrl = response.ssoUrl;

        if (redirectUrl) {

          // Make HTTP call to the SSO callback URL to get tokens
          this.http.get<LoginResponse>(redirectUrl).subscribe({

            next: (loginResponse) => {

              this.isLoading = false;

              // Store tokens in browser
              this.authService.setToken(loginResponse.token);
              this.authService.setRefreshToken(loginResponse.refreshToken);

              this.resetForm();

              this.showSuccessMessage('Inicio de sesión SSO exitoso');

              this.router.navigate(['/home']);
            },

            error: (error) => {

              this.isLoading = false;
              
              this.errorMessage = 'Error en la autenticación SSO';
              this.snackBar.open(this.errorMessage, 'Cerrar', {
                duration: 5000,
                horizontalPosition: 'center',
                verticalPosition: 'top',
                panelClass: ['error-snackbar']
              });
            }
          });
        }
      },

      error: (error) => {
        
        this.isLoading = false;
        
        this.errorMessage = 'Error al iniciar sesión SSO';
        this.snackBar.open(this.errorMessage, 'Cerrar', {
          duration: 5000,
          horizontalPosition: 'center',
          verticalPosition: 'top',
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  /**
   * Mark all fields in the form group as touched to trigger validation display
   */
  private markFormGroupTouched(formGroup: FormGroup): void {

    Object.values(formGroup.controls).forEach(control => {

      control.markAsTouched();

      if (control instanceof FormGroup) {

        this.markFormGroupTouched(control);
      }
    });
  }

  /**
   * Handle login errors
   */
  private handleLoginError(error: any): void {

    if (error.status === 401) {

      this.errorMessage = 'Credenciales incorrectas. Por favor, inténtalo de nuevo.';

    } else if (error.status === 0) {

      this.errorMessage = 'No se pudo conectar con el servidor. Verifica tu conexión.';

    } else if (error.error?.message) {

      this.errorMessage = error.error.message;

    } else {
      this.errorMessage = 'Ha ocurrido un error. Por favor, inténtalo más tarde.';
    }
  }

  /**
   * Show success notification
   */
  private showSuccessMessage(message: string): void {

    this.snackBar.open(message, 'Cerrar', {
      duration: 3000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: ['success-snackbar']
    });
  }

  /**
   * Reset form and clear validation states
   */
  private resetForm(): void {

    this.loginForm.reset({
      email: '',
      password: ''
    }, { emitEvent: false });  // Do not issue change events
    
    // Mark controls as untouched and unmodified
    Object.keys(this.loginForm.controls).forEach(key => {
      const control = this.loginForm.get(key);
      control?.setErrors(null);        // Clear specific errors
      control?.markAsPristine();       // Unedited
      control?.markAsUntouched();      // Not touched by the user
    });
  }

  /**
   * Get email field for template access
   */
  get email() {

    return this.loginForm.get('email');
  }

  /**
   * Get password field for template access
   */
  get password() {
    
    return this.loginForm.get('password');
  }
}
