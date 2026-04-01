import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService, LoginResponse } from '../../services/auth.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: jasmine.SpyObj<AuthService>;

  const mockLoginResponse: LoginResponse = {
    token: 'mock-token',
    refreshToken: 'mock-refresh-token',
    expiresIn: 3600,
    type: 'Bearer'
  };

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['login', 'logout', 'isAuthenticated']);

    await TestBed.configureTestingModule({
      declarations: [LoginComponent],
      imports: [
        ReactiveFormsModule,
        BrowserAnimationsModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
        MatSnackBarModule
      ],
      providers: [
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {

    expect(component).toBeTruthy();
  });

  it('should initialize form with empty values', () => {

    expect(component.loginForm.value.email).toBe('');
    expect(component.loginForm.value.password).toBe('');
  });

  it('should validate email field as required', () => {

    const emailControl = component.loginForm.get('email');
    emailControl?.setValue('');

    expect(emailControl?.valid).toBeFalse();
  });

  it('should validate email format', () => {

    const emailControl = component.loginForm.get('email');
    emailControl?.setValue('invalid-email');

    expect(emailControl?.hasError('email')).toBeTrue();
  });

  it('should validate password field as required', () => {

    const passwordControl = component.loginForm.get('password');
    passwordControl?.setValue('');

    expect(passwordControl?.valid).toBeFalse();
  });

  it('should call authService.login on form submission', () => {

    authService.login.and.returnValue(of(mockLoginResponse));

    component.loginForm.setValue({
      email: 'test@example.com',
      password: 'password123'
    });

    component.onSubmit();

    expect(authService.login).toHaveBeenCalled();
  });

  it('should handle login error', () => {

    const errorResponse = { status: 401, error: { message: 'Invalid credentials' } };

    authService.login.and.returnValue(throwError(() => errorResponse));

    component.loginForm.setValue({
      email: 'test@example.com',
      password: 'wrongpassword'
    });

    component.onSubmit();

    expect(component.errorMessage).toBe('Credenciales incorrectas. Por favor, inténtalo de nuevo.');
  });

  it('should toggle password visibility', () => {

    expect(component.hidePassword).toBeTrue();

    component.hidePassword = !component.hidePassword;
    
    expect(component.hidePassword).toBeFalse();
  });
});
