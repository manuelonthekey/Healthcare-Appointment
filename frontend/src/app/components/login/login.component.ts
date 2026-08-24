import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  message = '';
  error = '';

  constructor(private authService: AuthService) {}

  onLogin(email: string, pass: string) {
    this.error = '';
    this.message = '';
    this.authService.login(email, pass).subscribe({
      next: (res) => {
        this.message = 'Success! JWT Token Stored in LocalStorage: ' + res.token.substring(0, 30) + '...';
      },
      error: (err) => {
        this.error = 'Login Failed: ' + (err.status === 401 || err.status === 403 ? 'Invalid Credentials' : err.message);
      }
    });
  }
}
