import classNames from "classnames"
import { useCleaningSchedulerContext } from "../contexts/CleaningSchedulerContext";
import { isTask, Person, Task } from "../types";
import { useEffect, useState } from "react";
import { getAllPeople } from "../services/apiGet";
import { Alert } from "./Alert";
import { addTask } from "../services/apiPost";
import { SingleSuggestion } from "./SingleSuggestion";

type Props = {
    tasks: Task[] | undefined
}

export const Suggestions = (props: Props) => {
    const { tasks } = props;
    const { setCurrentTask } = useCleaningSchedulerContext();
    const [people, setPeople] = useState<Person[] | undefined>(undefined);
    const [, setError] = useState<string | null>(null);
    const [alert, setAlert] = useState<string | null>(null);

    useEffect(() => {
        const fetchPeople = async () => {
            try {
                const people = await getAllPeople()
                setPeople(people)
            } catch (error) {
                setError("Failed to load the data")
            }
        };
        fetchPeople();
    }, []);

    const taskInList = (taskName: string) => {
        return tasks?.some(task => task.name == taskName)
    }

    const addSuggestedTask = async (
        taskName: string,
        costs: number,
        lastDoneAt: number,
        isRepeated: boolean,
        minRepeat: number,
        maxRepeat: number
    ) => {
        if (people != undefined) {
            if (people.length != 0) {
                const newTask: Task = {
                    name: taskName,
                    costs: costs,
                    preferredAssignee: people[0],
                    isPreferredFixed: false,
                    lastDoneAt: lastDoneAt,
                    isRepeated: isRepeated,
                    minRepeatInterval: minRepeat,
                    maxRepeatInterval: maxRepeat
                }

                const result = await addTask(newTask)

                if (isTask(result)) {
                    setCurrentTask(result)
                } else {
                    setAlert(`${result.statusCode} ${result.statusText}`);
                }
            } else {
                setAlert("There need to be at least one person for suggested tasks.")
            }
        }
    };

    return <div className={
        classNames(
            "bg-[#4393c9]/80",
            "p-4",
            "rounded-lg",
            "text-[#082150]",
        )
    }>
        <p className="font-bold text-lg ">
            Suggestions</p>
        <p className="text-sm mb-2">Click to add the sugested task</p>
        {alert && <Alert text={alert} onClick={() => setAlert(null)} />}
        <div className={classNames(
            "text-sm",
            "space-y-2",
            "scrollable-container")}>
            <SingleSuggestion taskInList={taskInList} addSuggestedTask={addSuggestedTask} 
            taskName={"Grocery Shopping"} costs={30} lastDoneAt={1} isRepeated={true} minRepeat={1} maxRepeat={1} />
            <SingleSuggestion taskInList={taskInList} addSuggestedTask={addSuggestedTask} 
            taskName={"Cleaning Bathroom"} costs={60} lastDoneAt={1} isRepeated={false} minRepeat={0} maxRepeat={0} />
            <SingleSuggestion taskInList={taskInList} addSuggestedTask={addSuggestedTask} 
            taskName={"Hoovering"} costs={20} lastDoneAt={1} isRepeated={true} minRepeat={1} maxRepeat={2} />
            <SingleSuggestion taskInList={taskInList} addSuggestedTask={addSuggestedTask} 
            taskName={"Mopping Floor"} costs={60} lastDoneAt={1} isRepeated={false} minRepeat={0} maxRepeat={0} />
            <SingleSuggestion taskInList={taskInList} addSuggestedTask={addSuggestedTask} 
            taskName={"Window Cleaning"} costs={90} lastDoneAt={1} isRepeated={false} minRepeat={0} maxRepeat={0} />
        </div>
    </div>
}