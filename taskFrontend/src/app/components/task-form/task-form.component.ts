import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Task } from '../../model/task';
import { TaskService } from '../../services/task.service';
import { BehaviorSubject, catchError, EMPTY, Observable } from 'rxjs';
import { LogoutComponent } from "../logout/logout.component";
import { AlertService } from '../../helpers/alert-service';


@Component({
  selector: 'app-task-form',
  standalone: true,
  imports: [FormsModule, CommonModule, LogoutComponent],
  templateUrl: './task-form.component.html',
  styleUrls: ['./task-form.component.css'] 
})
export class TaskFormComponent {

  private tasksSubject: BehaviorSubject<Task[]> = new BehaviorSubject<Task[]>([]);
  tasks$: Observable<Task[]> = this.tasksSubject.asObservable();
  newTask: Task = { title: '', dueDate: '' };
  editingTask: Task | null = null;


  constructor(
    private taskService: TaskService,
    public alerts: AlertService
  ) {
        this.getTasks();
    }

  getTasks(): void {
    this.taskService.getAll().subscribe(tasks => {
      this.tasksSubject.next(tasks);
    }, err => {
      console.error('Failed to load tasks', err);
      this.alerts.error('Failed to load tasks.');
    });
  }
  
  onSubmit(): void {
    const title = this.newTask.title?.trim();

    if (!title) {
      this.alerts.error('Please enter a task title.');
      return;
    }

    this.taskService.save({...this.newTask})
      .subscribe({
        next: () => {
          this.alerts.success('Task added successfully');
          this.getTasks();
          this.newTask = { title: '', dueDate: '' };
        },
        error: err => {
          console.error('Error saving task', err);
          this.alerts.error('Failed to save task.');
        }
      });
  }

  startEdit(task: Task): void {
    this.editingTask = { ...task };
  }

  saveEdit(): void {

    if (!this.editingTask) return;

    const title = this.editingTask.title?.trim();
    if (!title) {
      this.alerts.error('Title cannot be empty.');
      return;
    }

    const patch: Task = {
      id: this.editingTask.id,
      title: title!,
      dueDate: this.editingTask.dueDate ?? null
    };

    const previous = this.tasksSubject.value.map(t => ({ ...t }));

    const optimisticallyUpdated = this.tasksSubject.value.map(t =>
      t.id === patch.id ? { ...t, title: patch.title, dueDate: patch.dueDate } : t
    );
    this.tasksSubject.next(optimisticallyUpdated);
    this.editingTask = null;

    this.taskService.update(patch).subscribe({
      next: () => {
        this.alerts.success('Task updated successfully');
      },
      error: err => {
        console.error('Update failed', err);
        this.tasksSubject.next(previous);
        this.alerts.error('Failed to update task. Your changes were reverted.');
      }
    });
  }

  cancelEdit(): void {
    this.editingTask = null;
  }

  deleteTask(task: Task): void {
    if (!task.id) return;
    if (!confirm(`Delete task "${task.title}"?`)) return;
    this.taskService.delete(task.id).subscribe({
      next: () => {
        console.log('Task deleted');
        this.getTasks();
      },
      error: err => {
        console.error('Delete failed', err);
        this.alerts.error('Failed to delete task.');
      }
    });
  }

}
