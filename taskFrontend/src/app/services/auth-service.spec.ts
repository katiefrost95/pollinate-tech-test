
import { TestBed } from '@angular/core/testing';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth-service';
import { firstValueFrom } from 'rxjs';
import { take } from 'rxjs/operators';

describe('AuthService (Angular v21, Vitest)', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const BASE_URL = 'http://localhost:8080';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('isLoggedIn()', () => {
    it('starts as false', async () => {
      const loggedIn = await firstValueFrom(service.isLoggedIn().pipe(take(1)));
      expect(loggedIn).toBe(false);
    });
  });

  describe('login()', () => {
    it('POSTs /login with withCredentials=true and sets auth to true on 200', async () => {
      const username = 'alice';
      const password = 'secret';

      const loginPromise = firstValueFrom(service.login(username, password).pipe(take(1)));

      const req = httpMock.expectOne(`${BASE_URL}/login`);
      expect(req.request.method).toBe('POST');
      expect(req.request.withCredentials).toBe(true);
      expect(req.request.body).toEqual({ username, password });

      req.flush({ response: 'ok' }, { status: 200, statusText: 'OK' });

      const ok = await loginPromise;
      expect(ok).toBe(true);

      const loggedIn = await firstValueFrom(service.isLoggedIn().pipe(take(1)));
      expect(loggedIn).toBe(true);
    });

    it('errors on non-2xx (e.g., 401) and auth remains false', async () => {
      const promise = firstValueFrom(service.login('charlie', 'badpwd').pipe(take(1)))
        .then(() => {
          throw new Error('Expected error, but got next()');
        })
        .catch((err) => err);

      const req = httpMock.expectOne(`${BASE_URL}/login`);
      expect(req.request.withCredentials).toBe(true);
      req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });

      const err = await promise;
      expect(err.status).toBe(401);

      const loggedIn = await firstValueFrom(service.isLoggedIn().pipe(take(1)));
      expect(loggedIn).toBe(false);
    });
  });

  describe('register()', () => {
    it('POSTs /register and returns token', async () => {
      const username = 'emma';
      const password = 'strong';

      const resPromise = firstValueFrom(service.register(username, password).pipe(take(1)));

      const req = httpMock.expectOne(`${BASE_URL}/register`);
      expect(req.request.method).toBe('POST');
      expect(req.request.withCredentials).toBeFalsy();
      expect(req.request.body).toEqual({ username, password });

      req.flush({ token: 'abc123' });

      const res = await resPromise;
      expect(res).toEqual({ token: 'abc123' });
    });
  });

  describe('logout()', () => {
    it('POSTs /logout with withCredentials=true, sets auth=false on success', async () => {
      const loginP = firstValueFrom(service.login('kate', 'ok').pipe(take(1)));

      const loginReq = httpMock.expectOne(`${BASE_URL}/login`);
      loginReq.flush({ response: 'ok' }, { status: 200, statusText: 'OK' });

      await loginP;

      const pre = await firstValueFrom(service.isLoggedIn().pipe(take(1)));
      expect(pre).toBe(true);

      const logoutP = firstValueFrom(service.logout().pipe(take(1)));

      const req = httpMock.expectOne(`${BASE_URL}/logout`);
      expect(req.request.method).toBe('POST');
      expect(req.request.withCredentials).toBe(true);
      expect(req.request.body).toBeNull();

      req.flush(null, { status: 204, statusText: 'No Content' });

      const ok = await logoutP;
      expect(ok).toBe(true);

      const post = await firstValueFrom(service.isLoggedIn().pipe(take(1)));
      expect(post).toBe(false);
    });

    it('errors on non-2xx (e.g., 500) and auth remains unchanged in error path', async () => {
      const initial = await firstValueFrom(service.isLoggedIn().pipe(take(1)));
      expect(initial).toBe(false);

      const logoutPromise = firstValueFrom(service.logout().pipe(take(1)))
        .then(() => {
          throw new Error('Expected error, but got next()');
        })
        .catch((err) => err);

      const req = httpMock.expectOne(`${BASE_URL}/logout`);
      expect(req.request.withCredentials).toBe(true);
      req.flush({ message: 'Server error' }, { status: 500, statusText: 'Server Error' });

      const err = await logoutPromise;
      expect(err.status).toBe(500);

      const loggedIn = await firstValueFrom(service.isLoggedIn().pipe(take(1)));
      expect(loggedIn).toBe(false);
    });
  });
});
