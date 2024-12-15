import { useEffect, useState } from "react";
import * as types from "../types";
import { getTask } from "../services/apiGet";
import classNames from "classnames";
import { useCleaningSchedulerContext } from "../contexts/CleaningSchedulerContext";
import { deleteTask } from "../services/apiDelete";
import { Alert } from "./Alert";
import { DeleteButton } from "./DeleteButton";
import { ChangeButton } from "./ChangeButton";
import { TaskPrompt } from "./TaskPrompt";

type Props = {
    taskName: string
}

export const Task = (props: Props) => {
    const { taskName } = props;
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [task, setTask] = useState<types.Task | undefined>(undefined)
    const [alert, setAlert] = useState<string | null>(null);
    const { setCurrentTask } = useCleaningSchedulerContext();
    const [isPopupVisible, setIsPopupVisible] = useState(false);

    const togglePopup = () => {
        setIsPopupVisible((prev) => !prev);
    };

    useEffect(() => {
        const fetchTask = async () => {
            try {
                setLoading(true)
                const task = await getTask(taskName)
                setTask(task)
            } catch (error) {
                setError("Failed to load the data")
            } finally {
                setLoading(false)
            }
        };
        fetchTask();
    }, [taskName]);

    const deleteCurrentTask = async () => {
        const result = await deleteTask(taskName)

        if (result == 204) {
            const fakePerson: types.Person = { name: "fakePerson", availabilityAssignment: new Map<number, number>() }
            const fakeTask: types.Task = {
                name: "fakeTask", costs: 0, preferredAssignee: fakePerson,
                isPreferredFixed: false, lastDoneAt: 1, isRepeated: false, minRepeatInterval: 0, maxRepeatInterval: 0
            }
            setCurrentTask(fakeTask);
            setTask(undefined);
        } else if (result == 409) {
            setAlert("At least schedule is dependent on this task. Delete these first.")
        } else {
            console.log("Something unforeseen went wrong...")
        }
    }

    if (loading) return <p>Loading</p>
    if (error) return <p>{error}</p>

    return <div className="w-full">
        {isPopupVisible && <TaskPrompt isPopupVisible={isPopupVisible} setIsPopupVisible={togglePopup}
            changePrompt={true} currentTask={task} />}
        {task ? (
            <div className={classNames(
                "rounded-[50px]",
                "mt-10",
                "p-10",
                "px-[10%]",
                "w-[70%]",
                "mx-auto",
                "bg-black/30",
                "text-[#082150]",
                "flex",
                "flex-col",
            )}>
                <h1 className="font-semibold mb-4 text-3xl">
                    Task: <br />
                    <div className="text-xl">
                        {task.name}
                    </div></h1>
                <table >
                    <tbody>
                        <tr><td><strong>Costs:</strong></td><td>{task.costs} minutes</td></tr>
                        <tr><td><strong>Preferred Assignee:</strong></td><td>{task.preferredAssignee.name}</td></tr>
                        <tr><td><strong>Is Preference Strict?</strong></td><td>{task.isPreferredFixed ? "Yes" : "No"}</td></tr>
                        <tr><td><strong>When Was Task Last done?</strong></td><td>Week {task.lastDoneAt}</td></tr>
                        <tr><td><strong>Is Task Repeated</strong></td><td>{task.isRepeated ? "Yes" : "No"}</td></tr>
                        {task.isRepeated && (
                            <>
                                <tr><td><strong>Minimum Time Between Task Repetitions:</strong></td><td>{task.minRepeatInterval == 1 ? task.minRepeatInterval + " week" : task.minRepeatInterval + " weeks"}</td></tr>
                                <tr><td><strong>Maximum Time Between Task Repetitions:</strong></td><td>{task.maxRepeatInterval == 1 ? task.maxRepeatInterval + " week" : task.maxRepeatInterval + " weeks"}</td></tr>
                            </>
                        )}
                        {!task.isRepeated &&
                            <tr><td><strong>Max # Weeks Before Task:</strong></td><td>{task.maxRepeatInterval == 1 ? task.maxRepeatInterval + " week" : task.maxRepeatInterval + " weeks"}</td></tr>
                        }
                    </tbody>
                </table>
                {alert && <Alert text={alert} onClick={() => setAlert(null)} />}
                <div className="w-full">
                    <ChangeButton text={"Change Task"} changeFunction={togglePopup} />
                    <DeleteButton text={"Delete Task"} deleteFunction={deleteCurrentTask} />
                </div>
            </div>
        ) : (
            <p className="p-10">No task found.</p>
        )}
    </div>
}