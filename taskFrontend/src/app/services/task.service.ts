import { Injectable } from '@angular/core';
import { AllTasksResponse, Task } from '../model/task';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private tasks: Task[] = [];

  private tasksUrl: string;

  constructor(private http: HttpClient) {
    this.tasksUrl = 'http://localhost:8080';
  }

  public getAll(): Observable<Task[]> {
    return this.http.get<AllTasksResponse>(`${this.tasksUrl}/tasks`, { withCredentials: true }).pipe(
      map(response => response.tasks)
    );
  }

  public save(task: Task) {
    return this.http.post<Task>(`${this.tasksUrl}/tasks`, task, { withCredentials: true });
  }

  public update(task: Task): Observable<Task> {
    if (!task.id) throw new Error('Task id is required for update');
    return this.http.put<Task>(`${this.tasksUrl}/tasks/${task.id}`, task, { withCredentials: true });
  }

  public delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.tasksUrl}/tasks/${id}`, { withCredentials: true });
  }

}