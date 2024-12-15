import { Person, Task } from "../types";

export async function addPerson(newPerson: Person) {
    const transformedPerson = {
        ...newPerson,
        availabilityAssignment: Object.fromEntries(newPerson.availabilityAssignment)
    }

    const response = await fetch(`cleaning-scheduler/api/addPerson`, {
        method: "POST",
        headers: {
            Accept: "application/json",
            "Content-Type": "application/json",
        },
        body: JSON.stringify(transformedPerson)
    });


    if (response.status == 200) {
        const person: Person = await response.json();

        return person;
    }
    else {
        return {
            statusCode: response.status,
            statusText: response.statusText
        };
    }
}

export async function addTask(newTask: Task) {

    const transformedTask = {
        ...newTask,
        preferredAssignee: {
            ...newTask.preferredAssignee,
            availabilityAssignment: Object.fromEntries(newTask.preferredAssignee.availabilityAssignment)
        }
    }

    const response = await fetch(`cleaning-scheduler/api/addTask`, {
        method: "POST",
        headers: {
            Accept: "application/json",
            "Content-Type": "application/json",
        },
        body: JSON.stringify(transformedTask)
    });

    if (response.status == 200) {
        const task: Task = await response.json();

        return task;
    }
    else {
        return {
            statusCode: response.status,
            statusText: response.statusText
        };
    }
}