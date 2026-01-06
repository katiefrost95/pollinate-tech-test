import { Component } from '@angular/core';
import { AuthService } from '../../services/auth-service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-logout',
  standalone: true,
  imports: [],
  templateUrl: './logout.component.html',
  styleUrl: './logout.component.css',
})
export class LogoutComponent {

  constructor(
    private router: Router,
    private authService: AuthService) { 
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        console.log('User logged out successfully');
        this.router.navigateByUrl('/login');
      },
      error: (err) => {
        console.error('Logout failed:', err);
        this.router.navigateByUrl('/login');
      }
    });

  }

}
