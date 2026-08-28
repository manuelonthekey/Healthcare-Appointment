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
  isPatient = false;
  pastAppointments: any[] = [];

  constructor(private authService: AuthService) {
    this.isAdmin = this.authService.hasRole('ADMIN');
    this.isDoctor = this.authService.hasRole('DOCTOR');
    this.isPatient = this.authService.hasRole('PATIENT');
  }

  ngOnInit() {
    if (this.isPatient) {
      // TODO(backend): No endpoint exists to specifically fetch past appointments. 
      // Wiring this up with dummy data to represent the UI for now.
      this.pastAppointments = [
        { id: 101, date: '2023-01-15', status: 'Completed' },
        { id: 102, date: '2023-05-20', status: 'Cancelled' }
      ];
    }
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

  logout() {
    this.authService.logout();
  }
}
