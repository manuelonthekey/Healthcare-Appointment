import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.scss'
})
export class AdminComponent implements OnInit {
  doctors: any[] = [];
  statusFilter: string = ''; // '' means 'ALL'
  isLoading = false;
  isUpdating = false;

  constructor(private http: HttpClient, private auth: AuthService) {}

  ngOnInit() {
    this.loadDoctors();
  }

  loadDoctors() {
    this.isLoading = true;
    let url = '/api/admin/doctors';
    if (this.statusFilter) {
      url += `?status=${this.statusFilter}`;
    }

    this.http.get<any[]>(url).subscribe({
      next: (res) => {
        this.doctors = res;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load doctors', err);
        this.isLoading = false;
        alert('Error loading doctors.');
      }
    });
  }

  onFilterChange(event: any) {
    this.statusFilter = event.target.value;
    this.loadDoctors();
  }

  updateStatus(doctor: any, newStatus: string) {
    if (!confirm(`Are you sure you want to change Dr. ${doctor.name}'s status to ${newStatus}?`)) {
      return;
    }

    this.isUpdating = true;
    this.http.put(`/api/admin/doctors/${doctor.id}/status`, { status: newStatus }).subscribe({
      next: () => {
        this.isUpdating = false;
        alert(`Status successfully updated to ${newStatus}`);
        this.loadDoctors(); // refresh list
      },
      error: (err) => {
        this.isUpdating = false;
        console.error('Failed to update status', err);
        alert('Error updating status.');
      }
    });
  }

  // ==== Add Doctor Flow ====
  showAddDoctorModal = false;
  newDoctor = { name: '', email: '', password: '', specialization: '', slotDurationMins: 30 };
  isAdding = false;

  openAddDoctorModal() {
    this.showAddDoctorModal = true;
    this.newDoctor = { name: '', email: '', password: '', specialization: '', slotDurationMins: 30 };
  }

  closeAddDoctorModal() {
    this.showAddDoctorModal = false;
  }

  addDoctor() {
    this.isAdding = true;
    this.http.post('/api/admin/doctors', this.newDoctor).subscribe({
      next: () => {
        this.isAdding = false;
        this.closeAddDoctorModal();
        this.loadDoctors();
        alert('Doctor added successfully!');
      },
      error: (err) => {
        this.isAdding = false;
        alert('Error adding doctor: ' + (err.error || 'Unknown error'));
      }
    });
  }

  // ==== Manage Schedule Flow ====
  showScheduleModal = false;
  selectedDoctorForSchedule: any = null;
  availabilities: any[] = [];
  daysOfWeek = [
    { value: 1, label: 'Monday' }, { value: 2, label: 'Tuesday' }, { value: 3, label: 'Wednesday' },
    { value: 4, label: 'Thursday' }, { value: 5, label: 'Friday' }, { value: 6, label: 'Saturday' }, { value: 7, label: 'Sunday' }
  ];

  openScheduleModal(doctor: any) {
    this.selectedDoctorForSchedule = doctor;
    this.showScheduleModal = true;
    this.loadSchedule(doctor.id);
  }

  closeScheduleModal() {
    this.showScheduleModal = false;
    this.selectedDoctorForSchedule = null;
  }

  loadSchedule(doctorId: number) {
    this.http.get<any[]>(`/api/doctors/${doctorId}/availability`).subscribe(res => {
      // Default to empty week if none
      if (res.length === 0) {
        this.availabilities = this.daysOfWeek.map(d => ({
          dayOfWeek: d.value,
          enabled: false,
          startTime: '09:00',
          endTime: '17:00'
        }));
      } else {
        this.availabilities = this.daysOfWeek.map(d => {
          const existing = res.find((r: any) => r.dayOfWeek === d.value);
          return existing 
            ? { dayOfWeek: d.value, enabled: true, startTime: existing.startTime.substring(0,5), endTime: existing.endTime.substring(0,5) }
            : { dayOfWeek: d.value, enabled: false, startTime: '09:00', endTime: '17:00' };
        });
      }
    });
  }

  saveSchedule() {
    const payload = this.availabilities
      .filter(a => a.enabled)
      .map(a => ({ dayOfWeek: a.dayOfWeek, startTime: a.startTime + ':00', endTime: a.endTime + ':00' }));
    
    this.isUpdating = true;
    this.http.put(`/api/admin/doctors/${this.selectedDoctorForSchedule.id}/working-hours`, payload).subscribe({
      next: () => {
        this.isUpdating = false;
        this.closeScheduleModal();
        alert('Schedule updated successfully');
      },
      error: () => {
        this.isUpdating = false;
        alert('Failed to update schedule');
      }
    });
  }

  // ==== Manage Leaves Flow ====
  showLeavesModal = false;
  selectedDoctorForLeaves: any = null;
  leaves: any[] = [];
  newLeaveDate: string = '';

  openLeavesModal(doctor: any) {
    this.selectedDoctorForLeaves = doctor;
    this.showLeavesModal = true;
    this.loadLeaves(doctor.id);
  }

  closeLeavesModal() {
    this.showLeavesModal = false;
    this.selectedDoctorForLeaves = null;
  }

  loadLeaves(doctorId: number) {
    this.http.get<any[]>(`/api/doctors/${doctorId}/leaves`).subscribe(res => this.leaves = res);
  }

  addLeave() {
    if (!this.newLeaveDate) return;
    this.isUpdating = true;
    this.http.post(`/api/admin/doctors/${this.selectedDoctorForLeaves.id}/leaves`, { leaveDate: this.newLeaveDate }).subscribe({
      next: () => {
        this.isUpdating = false;
        this.newLeaveDate = '';
        this.loadLeaves(this.selectedDoctorForLeaves.id);
      },
      error: () => {
        this.isUpdating = false;
        alert('Error adding leave');
      }
    });
  }

  deleteLeave(leaveId: number) {
    this.isUpdating = true;
    this.http.delete(`/api/admin/doctors/${this.selectedDoctorForLeaves.id}/leaves/${leaveId}`).subscribe({
      next: () => {
        this.isUpdating = false;
        this.loadLeaves(this.selectedDoctorForLeaves.id);
      },
      error: () => {
        this.isUpdating = false;
        alert('Error deleting leave');
      }
    });
  }
}
