export interface AllTasksResponse {
    tasks: Task[];
}

export interface Task {
    id?: number;
    title: string;
    dueDate: string | null;
}