import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ScheduleComponent } from './components/schedule/schedule.component';
import { LeavesComponent } from './components/leaves/leaves.component';
import { AdminComponent } from './components/admin/admin.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'schedule', component: ScheduleComponent },
  { path: 'leaves', component: LeavesComponent },
  { path: 'admin', component: AdminComponent },
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' }
];
