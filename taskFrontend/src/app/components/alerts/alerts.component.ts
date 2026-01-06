import { Component } from '@angular/core';
import { AlertService } from '../../helpers/alert-service';
import { CommonModule } from '@angular/common';
import { Observable } from 'rxjs';

export type AlertType = 'success' | 'error';
export interface Alert {
  type: AlertType;
  message: string;
}

@Component({
  selector: 'app-alerts',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './alerts.component.html',
  styleUrl: './alerts.component.css',
})
export class AlertsComponent {
  alert$: Observable<Alert | null>;
  // alert: Alert | null = null;

  constructor(private alertService: AlertService) {
    this.alert$ = this.alertService.alert$;
    // this.alertService.alert$.subscribe(a => (this.alert = a));
  }

}
