import classNames from "classnames";
import { Key, useEffect, useState } from "react";
import { AddButton } from "../components/AddButton";
import { SideNav } from "../components/SideNav";
import { Suggestions } from "../components/Suggestions";
import { Task } from "../components/Task";
import { TaskPrompt } from "../components/TaskPrompt";
import { useCleaningSchedulerContext } from "../contexts/CleaningSchedulerContext";
import { getAllTasks } from "../services/apiGet";
import * as types from "../types";

export const Tasks = () => {
    const [loading, setLoading] = useState<boolean>(true);
    const [currentTaskName, setCurrentTaskName] = useState<string | undefined>(undefined)
    const [, setError] = useState<string | null>(null);
    const [tasks, setTasks] = useState<types.Task[] | undefined>(undefined)
    const { currentTask } = useCleaningSchedulerContext()
    const [isPopupVisible, setIsPopupVisible] = useState(false);

    const togglePopup = () => {
        setIsPopupVisible((prev) => !prev);
    };

    useEffect(() => {
        const fetchTasks = async () => {
            try {
                setLoading(true)
                const tasks = await getAllTasks()
                setTasks(tasks)
                if (tasks?.length != 0) {
                    setCurrentTaskName(tasks![0].name)
                }
            } catch (error) {
                setError("Failed to load the data")
            } finally {
                setLoading(false)
            }
        };
        fetchTasks();
    }, [currentTask]);


    const showTask = (taskName: string) => {
        if (tasks != undefined) {
            var selectedTask = tasks[0];
            for (const task of tasks) {
                if (task.name == taskName) {
                    selectedTask = task
                }
            }
            setCurrentTaskName(selectedTask.name)
        }
    }

    return <>
        <aside className={classNames(
            "col-start-1",
            "bg-[#244788]",
            "flex",
            "flex-col",
            "items-center"
        )}>
            <SideNav includeSchedules={true} />
        </aside>
        <main className={classNames(
            "col-start-2",
            "bg-[#4393c9]",
            "flex",
            "justify-center"
        )}>
            {currentTaskName == undefined && <p className="m-4 mt-10">No task to show</p>}
            {currentTaskName != undefined && <Task taskName={currentTaskName} />}
            {isPopupVisible && <TaskPrompt isPopupVisible={isPopupVisible} setIsPopupVisible={togglePopup}
                changePrompt={false} currentTask={undefined} />}
        </main>
        <aside className={classNames(
            "col-start-3",
            "bg-[#244788]",
            "p-8",
            "font-medium",
        )}>
            <div className={
                classNames(
                    "bg-[#4393c9]/80",
                    "p-4",
                    "rounded-lg",
                    "text-[#082150]",
                    "mb-10"
                )
            }>
                <p className="font-bold text-lg ">
                    Tasks</p>
                <p className="text-sm mb-2">Click to see more information:</p>
                <div className={classNames(
                    "text-sm",
                    "space-y-2",
                    "scrollable-container"
                )}>
                    {loading && <p>Tasks loading</p>}
                    {!loading && tasks == undefined && <p>No tasks found</p>}
                    {tasks != undefined && tasks.map(task => (
                        <button
                            key={task.name as Key}
                            className={classNames(
                                "border-2 rounded-full",
                                "w-48",
                                "p-2",
                                "text-[#0f2057] ",
                                "border-[#0f2057]",
                                "bg-[#4393c9]",
                                { "!bg-[#0f2057]": currentTaskName == task.name },
                                { "text-[#4393c9]": currentTaskName == task.name },
                                "hover:text-[#4393c9] hover:bg-[#0f2057]",
                                "hover:border-[#0f2057] duration-300",
                            )}
                            disabled={currentTaskName == task.name}
                            onClick={() => showTask(task.name)}
                        >
                            {task.name}
                        </button>
                    ))}
                    <AddButton text={"Add New Task"} addFunction={togglePopup} />
                </div>
            </div>
            <Suggestions tasks={tasks}/>
        </aside>
    </>
}