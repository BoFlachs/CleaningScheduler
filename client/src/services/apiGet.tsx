import { parseTaskDTO } from "../parsers/parseDTO";
import { Person, Schedule, Task } from "../types";

export async function getPerson(name: string) {
    const response = await fetch(`cleaning-scheduler/api/getPerson?name=${name}`, {
        method: "GET",
        headers: {
            Accept: "application/json",
        },
    });

    if (response.status == 200) {
        const person = await response.json();

        return {
            ...person,

            availabilityAssignment: new Map(Object.entries(person.availabilityAssignment).map(
                ([key, value]) => [parseInt(key), value]
            )),
        } as Person
    }
    else {
        console.log(response)
        return undefined;
    }

}

export async function getAllPeople() {
    const response = await fetch(`cleaning-scheduler/api/getAllPeople`, {
        method: "GET",
        headers: {
            Accept: "application/json",
        },
    });


    if (response.status == 200) {
        const people: Person[] = await response.json();

        const personList = people.map((person) => ({
            ...person,
            availabilityAssignment: new Map(
                Object.entries(person.availabilityAssignment).map(
                    ([key, value]) => [parseInt(key), value]
                )
            ),
        }));

        return personList;
    }
    else {
        console.log(response)
        return undefined;
    }
}


export async function getTask(name: string) {
    const response = await fetch(`cleaning-scheduler/api/getTask?name=${name}`, {
        method: "GET",
        headers: {
            Accept: "application/json",
        },
    });

    if (response.status == 200) {
        const task = await response.json();

        return task as Task;
    }
    else {
        console.log(response)
        return undefined;
    }
}

export async function getAllTasks() {
    const response = await fetch(`cleaning-scheduler/api/getAllTasks`, {
        method: "GET",
        headers: {
            Accept: "application/json",
        },
    });


    if (response.status == 200) {
        const tasks: Task[] = await response.json();

        return tasks;
    }
    else {
        console.log(response)
        return undefined;
    }
}

export async function getSchedule(createdAt: string) {
    const dateAsString = createdAt
    const response = await fetch(`cleaning-scheduler/api/getSchedule?createdAt=${dateAsString}`, {
        method: "GET",
        headers: {
            Accept: "application/json",
        },
    });

    if (response.status == 200) {
        const schedule = await response.json();
        return {
            ...schedule,
            createdAt: new Date(schedule.createdAt),
            weekList: schedule.weekList.map((week: any) => ({
                ...week,
                taskAssignment: new Map(
                    Object.entries(week.taskAssignment).map(([taskKey, person]) => {
                        const task = parseTaskDTO(taskKey)

                        return [task, person]
                    })
                ),
            })),
        } as Schedule;
    }
    else {
        console.log(response)
        return undefined;
    }
}

export async function getAllSchedules() {
    const response = await fetch(`cleaning-scheduler/api/getAllSchedules`, {
        method: "GET",
        headers: {
            Accept: "application/json",
        },
    });


    if (response.status == 200) {
        const schedules: Schedule[] = await response.json();

        return schedules;
    }
    else {
        console.log(response)
        return undefined;
    }
}

export async function getNewSchedule(startWeek: number, 
    scheduleLength: number,
    isIntervalVariable: boolean,
    isBalanced: boolean
) {
    const response = await fetch(`cleaning-scheduler/api/newSchedule?startWeek=${startWeek}&scheduleLength=${scheduleLength}&isIntervalVariable=${isIntervalVariable}&isBalanced=${isBalanced}`, {
        method: "GET",
        headers: {
            Accept: "application/json"
        }
    });

    if (response.status == 200) {
        const schedule = await response.json();
        return {
            ...schedule,
            createdAt: new Date(schedule.createdAt),
            weekList: schedule.weekList.map((week: any) => ({
                ...week,
                taskAssignment: new Map(
                    Object.entries(week.taskAssignment).map(([taskKey, person]) => {
                        const task = parseTaskDTO(taskKey)

                        return [task, person]
                    })
                ),
            })),
        } as Schedule;
    }
    else {
        return {
            statusCode: response.status,
            statusText: response.statusText
        };
    }
}

export async function exportScheduleAsTxt(createdAt: string) {
    const dateAsString = createdAt
    const response = await fetch(`cleaning-scheduler/api/exportScheduleAsTxt?createdAt=${dateAsString}`, {
        method: "GET",
        headers: {
            Accept: "text/plain",
        },
    });

    if (response.status == 200) {
        const result = await response.text()
        return result
    }
    else {
        console.log(response)
        return "";
    }
}

export async function exportScheduleAsPDF(createdAt: string) {
    const dateAsString = createdAt
    const response = await fetch(`cleaning-scheduler/api/exportScheduleAsPDF?createdAt=${dateAsString}`, {
        method: "GET",
        headers: {
            Accept: "application/pdf",
        },
    });

    if (response.status == 200) {
        return response
    }
    else {
        console.log(response)
        return undefined;
    }
}