import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  message = '';
  error = '';

  constructor(private authService: AuthService, private router: Router) {}

  onRegister(email: string, pass: string, role: string) {
    this.error = '';
    this.message = '';
    this.authService.register({ email, password: pass, role }).subscribe({
      next: () => {
        this.message = 'Registered successfully!';
        setTimeout(() => this.router.navigate(['/login']), 1000);
      },
      error: (err) => {
        this.error = 'Registration Failed: ' + (err.error?.message || err.message);
      }
    });
  }
}
