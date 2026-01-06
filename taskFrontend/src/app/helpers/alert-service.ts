import { Injectable, NgZone } from '@angular/core';
import { BehaviorSubject, Observable, timer } from 'rxjs';
import { Alert } from '../components/alerts/alerts.component';

@Injectable({
  providedIn: 'root',
})
export class AlertService {

  private alertSubject = new BehaviorSubject<Alert | null>(null);
  public alert$: Observable<Alert | null> = this.alertSubject.asObservable();

  constructor(private ngZone: NgZone) {}

  success(message: string) {
    const timeoutMs = 3000;
    this.publish({ type: 'success', message }, timeoutMs);
  }

  error(message: string) {
    const timeoutMs = 5000;
    this.publish({ type: 'error', message }, timeoutMs);
  }

  private publish(alert: Alert, timeoutMs: number) {
    this.ngZone.run(() => {
      this.alertSubject.next(alert);
      timer(timeoutMs).subscribe(() => this.alertSubject.next(null));
    });
  }

}
