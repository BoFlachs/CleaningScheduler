export type Person = {
    name: string;
    availabilityAssignment: Map<number, number>;
}

export type Task = {
    name: string;
    costs: number;
    preferredAssignee: Person;
    isPreferredFixed: boolean;
    lastDoneAt: number;
    isRepeated: boolean;
    minRepeatInterval: number;
    maxRepeatInterval: number;
}

export type Week = {
    weekNumber: number;
    taskAssignment: Map<Task, Person>;
}

export type Schedule = {
    createdAt: Date;
    weekList: Week[];
    score: number;
}

export function isSchedule(schedule: unknown): schedule is Schedule {
    return (schedule as Schedule).weekList !== undefined;
}


export function isPerson(person: unknown): person is Person {
    return (person as Person).name !== undefined;
}


export function isTask(task: unknown): task is Task {
    return (task as Task).costs !== undefined;
}