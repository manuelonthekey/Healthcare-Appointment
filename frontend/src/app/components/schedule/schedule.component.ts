import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-schedule',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './schedule.component.html',
  styleUrl: './schedule.component.scss'
})
export class ScheduleComponent implements OnInit {
  availabilities: any[] = [];
  isSubmitting = false;

  newAvailability = {
    dayOfWeek: 1,
    startTime: '09:00',
    endTime: '17:00'
  };

  constructor(private http: HttpClient, private auth: AuthService) {}

  ngOnInit() {
    this.loadAvailabilities();
  }

  loadAvailabilities() {
    const profileId = localStorage.getItem('id');
    if (!profileId) return;

    this.http.get<any[]>(`/api/doctors/${profileId}/availability`).subscribe({
      next: (res) => this.availabilities = res,
      error: (err) => console.error('Failed to load availabilities', err)
    });
  }

  addAvailability() {
    if (this.newAvailability.startTime >= this.newAvailability.endTime) {
      alert('Start time must be before end time.');
      return;
    }

    this.isSubmitting = true;
    const profileId = localStorage.getItem('id');
    
    // Add seconds to time string as required by LocalTime format in Java
    const payload = {
      dayOfWeek: this.newAvailability.dayOfWeek,
      startTime: this.newAvailability.startTime + ':00',
      endTime: this.newAvailability.endTime + ':00'
    };

    this.http.post(`/api/doctors/${profileId}/availability`, payload).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.loadAvailabilities();
      },
      error: (err) => {
        this.isSubmitting = false;
        alert('Failed to add availability: ' + err.error);
      }
    });
  }

  deleteAvailability(id: number) {
    if (!confirm('Remove this schedule?')) return;
    
    const profileId = localStorage.getItem('id');
    this.http.delete(`/api/doctors/${profileId}/availability/${id}`).subscribe({
      next: () => this.loadAvailabilities(),
      error: (err) => alert('Failed to delete availability')
    });
  }

  getDayName(dayIndex: number): String {
    const days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
    return days[dayIndex - 1];
  }
}
