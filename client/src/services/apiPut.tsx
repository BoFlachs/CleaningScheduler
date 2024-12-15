
import { Person, Task } from "../types";

export async function changePerson(oldName: string, changedPerson: Person) {
    const transformedPerson = {
        ...changedPerson,
        availabilityAssignment: Object.fromEntries(changedPerson.availabilityAssignment)
    }

    const response = await fetch(`cleaning-scheduler/api/changePerson?oldName=${oldName}`, {
        method: "PUT",
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

export async function changeTask(oldName: string, changedTask: Task) {
    const tranformedPerson = {
        ...changedTask.preferredAssignee,
        availabilityAssignment: changedTask.preferredAssignee.availabilityAssignment
    }
    const transformedTask = {
        ...changedTask,
        preferredAssignee: tranformedPerson
    }

    const response = await fetch(`cleaning-scheduler/api/changeTask?oldName=${oldName}`, {
        method: "PUT",
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