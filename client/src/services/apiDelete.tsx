
export async function deletePerson(name: string) {
    const response = await fetch(`cleaning-scheduler/api/deletePerson?name=${name}`, {
        method: "DELETE"
    });

    if (response.status != 204 && response.status != 409) {
        console.log(response)
    }
    return response.status;
}

export async function deleteTask(name: string) {
    const response = await fetch(`cleaning-scheduler/api/deleteTask?name=${name}`, {
        method: "DELETE"
    });

    if (response.status != 204 && response.status != 409) {
        console.log(response)
    }
    return response.status;
}


export async function deleteSchedule(createdAt: string) {
    const response = await fetch(`cleaning-scheduler/api/deleteSchedule?createdAt=${createdAt}`, {
        method: "DELETE"
    });

    if (response.status != 204 && response.status != 409) {
        console.log(response)
    }
    return response.status;
}