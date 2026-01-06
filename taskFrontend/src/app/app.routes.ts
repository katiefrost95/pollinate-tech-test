import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { TaskFormComponent } from './components/task-form/task-form.component';
import { LoginComponent } from './components/login/login.component';
import { AuthGuard } from './guards/auth.guard';
import { LogoutComponent } from './components/logout/logout.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'addtask', component: TaskFormComponent, canActivate: [AuthGuard] },
  { path: 'login', component: LoginComponent },
  { path: 'logout', component: LogoutComponent },
  { path: '**', redirectTo: 'login' }
];