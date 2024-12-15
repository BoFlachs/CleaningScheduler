import classNames from "classnames";
import { useEffect, useState } from "react";
import Popup from "reactjs-popup";
import { Alert } from "./Alert";
import { isTask, Person, Task } from "../types";
import { addTask } from "../services/apiPost";
import { useCleaningSchedulerContext } from "../contexts/CleaningSchedulerContext";
import { getAllPeople } from "../services/apiGet";
import { changeTask } from "../services/apiPut";

type Props = {
    isPopupVisible: boolean,
    setIsPopupVisible: () => void,
    changePrompt: boolean,
    currentTask: Task | undefined
}

export const TaskPrompt = (props: Props) => {
    const { isPopupVisible, setIsPopupVisible, changePrompt, currentTask } = props;
    const [alert, setAlert] = useState<string | null>(null);
    const [taskName, setTaskName] = useState<string>(changePrompt ? currentTask!.name : "");
    const [costs, setCosts] = useState<number>(changePrompt ? currentTask!.costs : 0);
    const [prefName, setPrefName] = useState<string>(changePrompt ? currentTask!.preferredAssignee.name : "");
    const [currentPref, setCurrentPref] = useState<Person | undefined>(changePrompt ? currentTask!.preferredAssignee : undefined);
    const [isPrefFixed, setIsPrefFixed] = useState<boolean>(changePrompt ? currentTask!.isPreferredFixed : false);
    const [lastDoneAt, setLastDoneAt] = useState<number>(changePrompt ? currentTask!.lastDoneAt : 1);
    const [isRepeated, setIsRepeated] = useState<boolean>(changePrompt ? currentTask!.isRepeated : false);
    const [minRepeat, setMinRepeat] = useState<number>(changePrompt ? currentTask!.minRepeatInterval : 0);
    const [maxRepeat, setMaxRepeat] = useState<number>(changePrompt ? currentTask!.maxRepeatInterval : 0);
    const { setCurrentTask } = useCleaningSchedulerContext();
    const [loading, setLoading] = useState<boolean>(true);
    const [, setError] = useState<string | null>(null);
    const [people, setPeople] = useState<Person[] | undefined>(undefined);

    useEffect(() => {
        const fetchPeople = async () => {
            try {
                setLoading(true)
                const people = await getAllPeople()
                setPeople(people)
            } catch (error) {
                setError("Failed to load the data")
            } finally {
                setLoading(false)
            }
        };
        fetchPeople();
    }, []);


    const handleMinRepeatChange = (value: number) => {
        if (value <= maxRepeat) {
            setMinRepeat(value);
        }
    };

    const handleMaxRepeatChange = (value: number) => {
        if (value >= minRepeat) {
            setMaxRepeat(value);
        }
    };

    const handlePrefChange = (value: string) => {
        setPrefName(value);
        const person = people?.filter(person => person.name == value)?.[0]
        if (person != undefined) {
            setCurrentPref(person)
        }
    }

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (currentPref != undefined) {

            var result: Task | { statusCode: number, statusText: string }
            if (changePrompt) {
                const changedTask: Task = {
                    name: taskName,
                    costs: costs,
                    preferredAssignee: currentPref,
                    isPreferredFixed: isPrefFixed,
                    lastDoneAt: lastDoneAt,
                    isRepeated: isRepeated,
                    minRepeatInterval: minRepeat,
                    maxRepeatInterval: maxRepeat
                }

                result = await changeTask(currentTask!.name, changedTask)
            } else {
                const newTask: Task = {
                    name: taskName,
                    costs: costs,
                    preferredAssignee: currentPref,
                    isPreferredFixed: isPrefFixed,
                    lastDoneAt: lastDoneAt,
                    isRepeated: isRepeated,
                    minRepeatInterval: minRepeat,
                    maxRepeatInterval: maxRepeat
                }

                result = await addTask(newTask)
            }

            if (isTask(result)) {
                setCurrentTask(result)
                setIsPopupVisible()
            } else {
                setAlert(`${result.statusCode} ${result.statusText}`);
            }
        }
    };

    return <Popup open={isPopupVisible} modal >
        {alert && <Alert text={alert} onClick={() => setAlert(null)} />}
        <div
            className={classNames(
                "fixed top-0 bottom-0 left-0 right-0",
                "bg-black/20",
                "flex"
            )}>
            <div
                className={classNames(
                    "relative",
                    "p-6 rounded-lg shadow-lg bg-[#0f2057] m-auto",
                    "w-[550px]",
                )}>
                <button
                    className={classNames(
                        "absolute top-0 right-4 ",
                        "text-4xl",
                        "text-[#4393c9] hover:text-white"
                    )}
                    onClick={() => setIsPopupVisible()}>
                    &times;
                </button>
                <h2 className="text-xl text-[#4393c9] font-semibold mb-3">
                    {changePrompt ? "Change Task" : "Add New Task"}
                </h2>
                <form onSubmit={handleSubmit}>
                    <div className="grid grid-cols-[55%_40%] gap-4 m-4">
                        <label htmlFor="taskName" className="font-medium text-[#4393c9]">
                            Name:
                        </label>
                        <input
                            id="taskName"
                            value={taskName}
                            onChange={(e) => setTaskName(e.currentTarget.value)}
                            className="mt-1 p-2 border-[#4393c9] rounded-lg h-8 w-44 bg-[#4393c9]"
                            required />
                        <label htmlFor="costs" className="font-medium text-[#4393c9]">
                            Costs (in minutes):
                        </label>
                        <input
                            id="costs"
                            type="number"
                            min="0"
                            value={costs}
                            onChange={(e) => setCosts(Number(e.currentTarget.value))}
                            className="mt-1 p-2 border-[#4393c9] rounded-lg h-8 w-44 bg-[#4393c9]"
                            required />
                        <label htmlFor="preferredAssignee" className="block font-medium text-[#4393c9]">
                            Preferred Assignee:
                        </label>
                        <select
                            id="preferredAssignee"
                            value={prefName}
                            onChange={(e) => handlePrefChange(e.currentTarget.value)}
                            className="mt-1 p-2 border-[#4393c9] rounded-lg w-44 bg-[#4393c9]"
                            required>
                            <option value="">Select Person</option>
                            {loading && <option value="">Loading</option>}
                            {people != undefined &&
                                Array.from(people, (person, index) => (
                                    <option key={index + 1} value={person.name}>
                                        {person.name}
                                    </option>
                                ))}
                        </select>
                        <label htmlFor="isPrefFixed" className="block font-medium text-[#4393c9]">
                            Is Preferred Assignee Fixed:
                        </label>
                        <input type="checkbox"
                            id="isPrefFixed"
                            value={String(isPrefFixed)}
                            checked={isPrefFixed}
                            className="bg-[#4393c9] my-auto"
                            onChange={(e) => setIsPrefFixed(e.currentTarget.checked)}
                        ></input>
                        <label htmlFor="lastDoneAt" className="block font-medium text-[#4393c9]">
                            Task Last Done At:
                        </label>
                        <select
                            id="lastDoneAt"
                            value={lastDoneAt}
                            onChange={(e) => setLastDoneAt(Number(e.currentTarget.value))}
                            className="mt-1 p-2 border-[#4393c9] rounded-lg w-28 bg-[#4393c9]"
                            required>
                            <option value="">Select Week</option>
                            {Array.from({ length: 52 }, (_, index) => (
                                <option key={index + 1} value={index + 1}>
                                    Week {index + 1}
                                </option>
                            ))}
                        </select>
                        <label htmlFor="isRepeated" className="block font-medium text-[#4393c9]">
                            Is Task Repeated:
                        </label>
                        <input type="checkbox"
                            id="isRepeated"
                            value={String(isRepeated)}
                            checked={isRepeated}
                            className="bg-[#4393c9] my-auto"
                            onChange={(e) => setIsRepeated(e.currentTarget.checked)}
                        ></input>
                        {isRepeated && <>
                            <label htmlFor="minRepeatInterval" className="font-medium text-[#4393c9]">
                                Minimum # Weeks Between Repetitions:
                            </label>
                            <input
                                id="minRepeatInterval"
                                type="number"
                                min="0"
                                value={minRepeat}
                                onChange={(e) => handleMinRepeatChange(Number(e.currentTarget.value))}
                                className="mt-1 p-2 border-[#4393c9] rounded-lg h-8 w-44 bg-[#4393c9]"
                                required />
                            <label htmlFor="maxRepeatInterval" className="font-medium text-[#4393c9]">
                                Maximum # Weeks Between Repetitions:
                            </label>
                            <input
                                id="maxRepeatInterval"
                                type="number"
                                min="0"
                                value={maxRepeat}
                                onChange={(e) => handleMaxRepeatChange(Number(e.currentTarget.value))}
                                className="mt-1 p-2 border-[#4393c9] rounded-lg h-8 w-44 bg-[#4393c9]"
                                required />
                        </>}
                        {!isRepeated && <>
                            <label htmlFor="singleRepeat" className="font-medium text-[#4393c9]">
                                Max # Weeks Before Task:
                            </label>
                            <input
                                id="singleRepeat"
                                type="number"
                                min="0"
                                value={maxRepeat}
                                onChange={(e) => setMaxRepeat(Number(e.currentTarget.value))}
                                className="mt-1 p-2 border-[#4393c9] rounded-lg h-8 w-44 bg-[#4393c9]"
                                required />
                        </>
                        }
                    </div>


                    <div className="flex justify-center">
                        <button
                            type="submit"
                            className="w-full bg-blue-600 text-[#0f2057] py-2 px-4 rounded-lg hover:bg-blue-700"
                        >
                            {changePrompt ? "Save changes" : "Add Task"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </Popup>
}