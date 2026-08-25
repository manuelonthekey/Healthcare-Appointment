import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { BookingModalComponent } from '../components/booking-modal/booking-modal.component';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, BookingModalComponent],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.scss'
})
export class LayoutComponent {
  isBookingModalOpen = false;
  isSidebarOpen = false;
  isAdmin = false;
  isDoctor = false;

  constructor(private authService: AuthService) {
    this.isAdmin = this.authService.hasRole('ADMIN');
    this.isDoctor = this.authService.hasRole('DOCTOR');
  }

  openBookingModal() {
    this.isBookingModalOpen = true;
  }

  closeBookingModal() {
    this.isBookingModalOpen = false;
  }

  toggleSidebar() {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  closeSidebar() {
    if (this.isSidebarOpen) {
      this.isSidebarOpen = false;
    }
  }
}
