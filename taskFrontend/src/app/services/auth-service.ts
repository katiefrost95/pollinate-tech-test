import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, shareReplay, tap } from 'rxjs';
import { BehaviorSubject, Observable } from 'rxjs';

export interface AuthResponse {
    response: string
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private tasksUrl: string;
  private authSubject = new BehaviorSubject<boolean>(false);

  constructor(private http: HttpClient) {
    this.tasksUrl = 'http://localhost:8080';
  }

  login(username: string, password: string) {
    return this.http.post<AuthResponse>(
      `${this.tasksUrl}/login`,
      { username, password },
      { withCredentials: true, observe: 'response' as 'response' }
    ).pipe(
      map((res: HttpResponse<AuthResponse>) => {
        console.log('Login response:', res);
        const ok = res.status === 200;
        if (ok) this.authSubject.next(true);
        return ok
      })
    );
  }

  register(username: string, password: string) {
    return this.http.post<{ token: string }>(`${this.tasksUrl}/register`, { username, password }).pipe(
      tap(res => {
        console.log('Register response:', res);
      })
    );
  }


  logout() {
    return this.http.post(`${this.tasksUrl}/logout`, null, {
      withCredentials: true,
      observe: 'response'
    }).pipe(
      map((resp: HttpResponse<unknown>) => {
        const ok = resp.status === 204;
        this.authSubject.next(false);
        return ok;
      })
    );
  }

  isLoggedIn(): Observable<boolean> {
    return this.authSubject.asObservable();
  }
}