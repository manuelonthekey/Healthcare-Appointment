import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-leaves',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './leaves.component.html',
  styleUrl: './leaves.component.scss'
})
export class LeavesComponent implements OnInit {
  leaves: any[] = [];
  newLeaveDate: string = '';
  isSubmitting = false;

  constructor(private http: HttpClient, private auth: AuthService) {}

  ngOnInit() {
    this.loadLeaves();
  }

  loadLeaves() {
    const profileId = localStorage.getItem('id');
    if (!profileId) return;

    this.http.get<any[]>(`/api/doctors/${profileId}/leaves`).subscribe({
      next: (res) => this.leaves = res,
      error: (err) => console.error('Failed to load leaves', err)
    });
  }

  addLeave() {
    if (!this.newLeaveDate) return;

    this.isSubmitting = true;
    const profileId = localStorage.getItem('id');
    
    const payload = {
      leaveDate: this.newLeaveDate
    };

    this.http.post(`/api/doctors/${profileId}/leaves`, payload).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.newLeaveDate = '';
        this.loadLeaves();
      },
      error: (err) => {
        this.isSubmitting = false;
        alert('Failed to add leave: ' + err.error);
      }
    });
  }

  deleteLeave(id: number) {
    if (!confirm('Cancel this leave?')) return;
    
    const profileId = localStorage.getItem('id');
    this.http.delete(`/api/doctors/${profileId}/leaves/${id}`).subscribe({
      next: () => this.loadLeaves(),
      error: (err) => alert('Failed to delete leave')
    });
  }
}
