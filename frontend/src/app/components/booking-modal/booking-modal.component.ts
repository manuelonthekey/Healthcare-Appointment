import { Component, EventEmitter, Output, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-booking-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './booking-modal.component.html',
  styleUrl: './booking-modal.component.scss'
})
export class BookingModalComponent implements OnInit, OnDestroy {
  @Output() close = new EventEmitter<void>();

  step = 1;
  doctors: any[] = [];
  selectedDoctor: any = null;
  selectedDate: string = '';
  selectedSlot: string = '';
  symptoms: string = '';
  
  // Timer state
  timerSeconds = 600;
  timerDisplay = '10:00';
  private timerInterval: any;

  // AI State (Task 7)
  isAnalyzing = false;
  aiAnalysis: any = null;
  private symptomsSubject = new Subject<string>();
  private symptomsSubscription!: Subscription;

  // Search State
  searchName: string = '';
  searchSpecialty: string = '';
  searchDate: string = '';

  constructor(private http: HttpClient, private authService: AuthService) {}

  ngOnInit() {
    this.searchDoctors();

    // Setup AI Debouncer
    this.symptomsSubscription = this.symptomsSubject
      .pipe(
        debounceTime(1500),
        distinctUntilChanged()
      )
      .subscribe(symptomsText => {
        if (symptomsText.length > 10) {
          this.triggerAiAnalysis(symptomsText);
        } else {
          this.aiAnalysis = null; // Clear if too short
        }
      });
  }

  searchDoctors() {
    let params = [];
    if (this.searchName) params.push(`name=${encodeURIComponent(this.searchName)}`);
    if (this.searchSpecialty) params.push(`specialty=${encodeURIComponent(this.searchSpecialty)}`);
    if (this.searchDate) params.push(`date=${encodeURIComponent(this.searchDate)}`);
    let query = params.length ? '?' + params.join('&') : '';
    
    this.http.get<any[]>(`/api/doctors${query}`).subscribe(res => {
      this.doctors = res;
    });
  }

  ngOnDestroy() {
    if (this.symptomsSubscription) {
      this.symptomsSubscription.unsubscribe();
    }
    this.clearTimer();
  }

  closeModal() {
    this.close.emit();
  }

  nextStep() {
    if (this.step === 2) {
      this.startTimer();
    }
    this.step++;
  }

  availableSlots: string[] = [];

  selectDoctor(doctor: any) {
    this.selectedDoctor = doctor;
    this.selectedSlot = '';
    this.fetchSlots();
  }

  onDateChange() {
    this.selectedSlot = '';
    this.fetchSlots();
  }

  fetchSlots() {
    if (this.selectedDoctor && this.selectedDate) {
      this.http.get<string[]>(`/api/doctors/${this.selectedDoctor.id}/slots?date=${this.selectedDate}`)
        .subscribe({
          next: (res) => this.availableSlots = res,
          error: (err) => {
            console.error(err);
            this.availableSlots = [];
          }
        });
    }
  }

  selectSlot(time: string) {
    this.selectedSlot = time;
  }

  startTimer() {
    this.timerSeconds = 600;
    this.updateTimerDisplay();
    this.timerInterval = setInterval(() => {
      this.timerSeconds--;
      this.updateTimerDisplay();
      if (this.timerSeconds <= 0) {
        this.clearTimer();
        this.closeModal(); // Time expired
      }
    }, 1000);
  }

  clearTimer() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
  }

  updateTimerDisplay() {
    const m = Math.floor(this.timerSeconds / 60);
    const s = this.timerSeconds % 60;
    this.timerDisplay = `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  }

  onSymptomsChange(text: string) {
    this.symptoms = text;
    this.symptomsSubject.next(text);
  }

  triggerAiAnalysis(text: string) {
    this.isAnalyzing = true;
    
    // Call the actual backend MS4 endpoint
    this.http.post<any>('/api/ai/analyze-symptoms', { symptoms: text })
      .subscribe({
        next: (response) => {
          this.aiAnalysis = response;
          this.isAnalyzing = false;
        },
        error: (err) => {
          console.error('AI Analysis failed', err);
          this.isAnalyzing = false;
          // Fallback UI
          this.aiAnalysis = {
            urgencyLevel: 'UNKNOWN',
            chiefComplaint: 'Service Unavailable',
            extractedSymptoms: []
          };
        }
      });
  }

  confirmBooking() {
    this.clearTimer();
    const patientId = localStorage.getItem('id');
    const doctorId = this.selectedDoctor.id;
    // selectedSlot is "HH:mm:ss"
    const datetime = `${this.selectedDate}T${this.selectedSlot}`;

    const holdRequest = {
      doctorId,
      patientId,
      datetime,
      symptoms: this.symptoms,
      aiAnalysis: this.aiAnalysis ? JSON.stringify(this.aiAnalysis) : null
    };

    // Note: To be fully robust, hold should be done when slot is selected.
    // In this UI flow, we confirm everything in one go at the end.
    this.http.post<any>('/api/patients/hold', holdRequest).subscribe({
      next: (hold) => {
        this.http.post<any>(`/api/patients/confirm/${hold.id}?patientId=${patientId}`, {}).subscribe({
          next: () => this.step = 4, // Success
          error: (err) => console.error("Confirm failed", err)
        });
      },
      error: (err) => console.error("Hold failed", err)
    });
  }
}
