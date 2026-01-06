import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth-service';
import { AlertService } from '../../helpers/alert-service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent {
  form:FormGroup;

    constructor(private fb:FormBuilder, 
                 private authService: AuthService,
                 public alerts: AlertService,    
                 private router: Router) {

        this.form = this.fb.group({
            username: ['',Validators.required],
            password: ['',Validators.required]
        });
    }

    login() {
        const val = this.form.value;
        if (val.username && val.password) {
            this.authService.login(val.username, val.password).subscribe({
                next: ok => {
                    if (ok) {
                    console.log('User is logged in');
                    this.router.navigateByUrl('/addtask')
                        .catch(err => console.error('Navigation error:', err));
                    } else {
                    console.warn('Login failed');
                    }
                },
                error: err => {
                    console.error('Login error:', err);
                    this.alerts.error('Login failed. Check you are registered and try again.');
                }
            });
        }
    }

    register() {
      const val = this.form.value;

        if (val.username && val.password) {
          this.authService.register(val.username, val.password)
              .subscribe({
                    next: () => {
                        console.log("User is registered");
                        this.alerts.success("Registration successful. Please Login");
                        this.router.navigate(['/login']).catch(err => console.error('Navigation error:', err));
                    },
                    error: err => {
                        console.error('Registration error:', err);
                        this.alerts.error('Registration failed. You may already be registered.');
                    }
              });
        }
    }
}
