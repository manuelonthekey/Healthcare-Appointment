import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  isDetailsModalOpen = false;
  metrics: any = null;
  todayAppointments: any[] = [];
  upcomingAppointments: any[] = [];
  medications: any[] = [];
  isPatient = false;
  
  selectedAppointmentId: number | null = null;

  // Post-visit AI State
  rawNotes = '';
  isSummarizing = false;
  aiSummary: any = null;

  constructor(private http: HttpClient, private authService: AuthService) {
    this.isPatient = this.authService.hasRole('PATIENT');
  }

  ngOnInit() {
    this.loadData();
  }

  searchQuery = '';
  statusFilter = '';

  get filteredUpcomingAppointments() {
    return this.upcomingAppointments.filter(apt => {
      const matchSearch = this.searchQuery ? apt.patientName?.toLowerCase().includes(this.searchQuery.toLowerCase()) : true;
      const matchStatus = this.statusFilter ? apt.status?.toLowerCase() === this.statusFilter.toLowerCase() : true;
      return matchSearch && matchStatus;
    });
  }

  loadData() {
    this.http.get<any>('/api/dashboard/metrics').subscribe(res => this.metrics = res);
    this.http.get<any[]>('/api/dashboard/today').subscribe(res => this.todayAppointments = res);
    this.http.get<any[]>('/api/dashboard/upcoming').subscribe(res => this.upcomingAppointments = res);
    if (this.isPatient) {
      this.http.get<any[]>('/api/patients/medications').subscribe(res => this.medications = res);
    }
  }

  openDetailsModal(apt: any) {
    this.selectedAppointmentId = apt.id;
    this.isDetailsModalOpen = true;
  }

  closeDetailsModal() {
    this.isDetailsModalOpen = false;
    this.selectedAppointmentId = null;
    this.aiSummary = null;
    this.rawNotes = '';
  }

  cancelAppointment() {
    if (!this.selectedAppointmentId) return;
    if (!confirm('Are you sure you want to cancel this appointment?')) return;
    
    this.http.post(`/api/appointments/${this.selectedAppointmentId}/cancel`, {}).subscribe({
      next: () => {
        alert('Appointment cancelled successfully.');
        this.closeDetailsModal();
        this.loadData();
      },
      error: (err) => {
        alert('Error cancelling appointment: ' + err.error);
      }
    });
  }

  completeAppointment() {
    if (!this.selectedAppointmentId) return;
    if (!this.aiSummary) {
      alert('Please generate AI summary first.');
      return;
    }

    const payload = {
      clinicalNotes: this.rawNotes,
      aiSummary: JSON.stringify(this.aiSummary)
    };

    this.http.post(`/api/appointments/${this.selectedAppointmentId}/complete`, payload).subscribe({
      next: () => {
        alert('Appointment completed and notes saved successfully!');
        this.closeDetailsModal();
        this.loadData();
      },
      error: (err) => {
        alert('Error completing appointment: ' + err.error);
      }
    });
  }

  draftSummaryWithAi() {
    this.isSummarizing = true;
    this.http.post<any>('/api/ai/summarize-notes', { notes: this.rawNotes })
      .subscribe({
        next: (res) => {
          this.aiSummary = res;
          this.isSummarizing = false;
        },
        error: (err) => {
          console.error(err);
          this.isSummarizing = false;
          this.aiSummary = {
            structuredSummary: 'AI Summarization Unavailable.',
            keyTakeaways: []
          };
        }
      });
  }
}
