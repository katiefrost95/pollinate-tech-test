import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AuthService } from '../services/auth-service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}

  canActivate(): boolean | UrlTree | Observable<boolean | UrlTree> {
    const result = this.auth.isLoggedIn();
    return (result as Observable<boolean>).pipe(
      map(isLogged => (isLogged ? true : this.router.createUrlTree(['/login'])))
    );
  }
}