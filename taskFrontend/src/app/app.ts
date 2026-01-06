import { Component, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, RouterOutlet } from '@angular/router';
import { AlertsComponent } from './components/alerts/alerts.component';    

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ReactiveFormsModule, RouterOutlet, RouterModule, AlertsComponent],
  template: `
    <app-alerts></app-alerts>
    <div class="container">
      <router-outlet></router-outlet>
    </div>
  `,
  styles: [`
    .container {
      max-width: 600px;
      margin: 0 auto;
      padding: 20px;
      font-family: Arial, sans-serif;
    }
    h1 {
      text-align: center;
      color: #333;
    }
  `]
})
export class App {
  protected readonly title = signal('taskFrontend');
}
