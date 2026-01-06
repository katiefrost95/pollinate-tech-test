
import { TestBed } from '@angular/core/testing';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TaskService } from './task.service'; // adjust the import path as needed
import { firstValueFrom } from 'rxjs';
import { take } from 'rxjs/operators';
import type { AllTasksResponse, Task } from '../model/task';

describe('TaskService (Angular v21, Vitest)', () => {
  let service: TaskService;
  let httpMock: HttpTestingController;

  const BASE_URL = 'http://localhost:8080';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TaskService, provideHttpClientTesting()],
    });

    service = TestBed.inject(TaskService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('getAll()', () => {
    it('GETs /tasks with withCredentials=true and maps response.tasks', async () => {
      const sampleTasks: Task[] = [
        { id: 1, title: 'Pay bills', dueDate: '2026-01-31' },
        { id: 2, title: 'Book flights', dueDate: null },
      ];

      const p = firstValueFrom(service.getAll().pipe(take(1)));

      const req = httpMock.expectOne(`${BASE_URL}/tasks`);
      expect(req.request.method).toBe('GET');
      expect(req.request.withCredentials).toBe(true);

      const payload: AllTasksResponse = { tasks: sampleTasks };
      req.flush(payload);

      const tasks = await p;
      expect(tasks).toEqual(sampleTasks);
    });

    it('propagates HTTP errors', async () => {
      const p = firstValueFrom(service.getAll().pipe(take(1)))
        .then(() => {
          throw new Error('Expected error but got next()');
        })
        .catch((err) => err);

      const req = httpMock.expectOne(`${BASE_URL}/tasks`);
      expect(req.request.method).toBe('GET');
      req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });

      const err = await p;
      expect(err.status).toBe(401);
    });
  });

  describe('save()', () => {
    it('POSTs /tasks with task body and withCredentials=true', async () => {
      const newTask: Task = { title: 'Write report', dueDate: '2026-02-10' };
      const returned: Task = { id: 10, title: 'Write report', dueDate: '2026-02-10' };

      const p = firstValueFrom(service.save(newTask).pipe(take(1)));

      const req = httpMock.expectOne(`${BASE_URL}/tasks`);
      expect(req.request.method).toBe('POST');
      expect(req.request.withCredentials).toBe(true);
      expect(req.request.body).toEqual(newTask);

      req.flush(returned);

      const res = await p;
      expect(res).toEqual(returned);
    });

    it('propagates HTTP errors on save', async () => {
      const newTask: Task = { title: 'Bad task', dueDate: null };

      const p = firstValueFrom(service.save(newTask).pipe(take(1)))
        .then(() => {
          throw new Error('Expected error but got next()');
        })
        .catch((err) => err);

      const req = httpMock.expectOne(`${BASE_URL}/tasks`);
      expect(req.request.method).toBe('POST');
      req.flush({ message: 'Bad Request' }, { status: 400, statusText: 'Bad Request' });

      const err = await p;
      expect(err.status).toBe(400);
    });
  });

  describe('update()', () => {
    it('throws synchronously when task.id is missing', () => {
      const badTask: Task = { title: 'No ID', dueDate: null };
      expect(() => service.update(badTask)).toThrowError('Task id is required for update');

      httpMock.expectNone(`${BASE_URL}/tasks/`);
    });

    it('PUTs /tasks/:id with body and withCredentials=true', async () => {
      const task: Task = { id: 7, title: 'Refactor code', dueDate: '2026-03-01' };
      const updated: Task = { ...task, title: 'Refactor code (phase 2)' };

      const p = firstValueFrom(service.update(task).pipe(take(1)));

      const req = httpMock.expectOne(`${BASE_URL}/tasks/${task.id}`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.withCredentials).toBe(true);
      expect(req.request.body).toEqual(task);

      req.flush(updated);

      const res = await p;
      expect(res).toEqual(updated);
    });

    it('propagates HTTP errors on update', async () => {
      const task: Task = { id: 99, title: 'Ghost task', dueDate: null };

      const p = firstValueFrom(service.update(task).pipe(take(1)))
        .then(() => {
          throw new Error('Expected error but got next()');
        })
        .catch((err) => err);

      const req = httpMock.expectOne(`${BASE_URL}/tasks/${task.id}`);
      expect(req.request.method).toBe('PUT');
      req.flush({ message: 'Not Found' }, { status: 404, statusText: 'Not Found' });

      const err = await p;
      expect(err.status).toBe(404);
    });
  });

  describe('delete()', () => {
    it('DELETEs /tasks/:id with withCredentials=true', async () => {
      const id = 5;

      const p = firstValueFrom(service.delete(id).pipe(take(1)));

      const req = httpMock.expectOne(`${BASE_URL}/tasks/${id}`);
      expect(req.request.method).toBe('DELETE');
      expect(req.request.withCredentials).toBe(true);
      expect(req.request.body).toBeNull();

      req.flush(null, { status: 204, statusText: 'No Content' });

      const res = await p;
      expect(res).toBeNull();
    });

    it('propagates HTTP errors on delete', async () => {
      const id = 42;

      const p = firstValueFrom(service.delete(id).pipe(take(1)))
        .then(() => {
          throw new Error('Expected error but got next()');
        })
        .catch((err) => err);

      const req = httpMock.expectOne(`${BASE_URL}/tasks/${id}`);
      expect(req.request.method).toBe('DELETE');
      req.flush({ message: 'Forbidden' }, { status: 403, statusText: 'Forbidden' });

      const err = await p;
      expect(err.status).toBe(403);
    });
  });
});
