import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-account',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './account.component.html',
  styleUrl: './account.component.scss'
})
export class AccountComponent implements OnInit {
  isPatient = false;
  isDoctor = false;

  // Form model
  profile = {
    name: '',
    phone: '',
    dob: '',
    specialization: '',
    medications: '',
    notes: ''
  };

  message = '';
  error = '';
  summaries: any[] = [];

  constructor(private authService: AuthService, private http: HttpClient) {
    this.isPatient = this.authService.hasRole('PATIENT');
    this.isDoctor = this.authService.hasRole('DOCTOR');
  }

  ngOnInit() {
    // Load latest summary to allow import
    if (this.isPatient) {
      // We don't have a specific endpoint for past summaries alone, but we could fetch appointments.
      // TODO(backend): Need a proper endpoint to fetch patient's own profile and summaries.
      // For now we'll just mock the structure to show the UI works if the data existed.
      this.summaries = [
        {
          id: 1,
          date: '2023-10-01',
          aiSummary: {
            medications: ['Lisinopril 10mg'],
            notes: 'Patient reports mild headaches. Advised to stay hydrated.'
          }
        }
      ];
    }
  }

  importFromSummary(summary: any) {
    if (summary && summary.aiSummary) {
      if (summary.aiSummary.medications) {
        this.profile.medications = summary.aiSummary.medications.join(', ');
      }
      if (summary.aiSummary.notes) {
        this.profile.notes = summary.aiSummary.notes;
      }
      this.message = 'Data imported from summary. Please review and save.';
      this.error = '';
    }
  }

  saveProfile() {
    this.message = '';
    this.error = '';
    const userId = localStorage.getItem('id');
    
    // TODO(backend): no endpoint yet.
    // There is currently no PUT /api/patients/{id}/profile or /api/doctors/{id}/profile endpoint.
    // Making a dummy HTTP call that will fail to demonstrate the wiring.
    const endpoint = this.isPatient ? `${environment.apiUrl}/api/patients/${userId}/profile` : `${environment.apiUrl}/api/doctors/${userId}/profile`;
    
    this.http.put(endpoint, this.profile).subscribe({
      next: () => {
        this.message = 'Profile saved successfully!';
      },
      error: (err) => {
        this.error = 'Failed to save profile (Endpoint does not exist). TODO(backend): Implement profile update endpoint.';
        console.warn('// TODO(backend): no endpoint yet for updating profile.', err);
      }
    });
  }
}
