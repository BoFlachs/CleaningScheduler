import classNames from "classnames";
import { useState } from "react";
import Popup from "reactjs-popup";
import { useCleaningSchedulerContext } from "../contexts/CleaningSchedulerContext";
import { getNewSchedule } from "../services/apiGet";
import { isSchedule } from "../types";
import { Alert } from "./Alert";

type Props = {
    isPopupVisible: boolean,
    setIsPopupVisible: () => void
}

export const SchedulePrompt = (props: Props) => {
    const { isPopupVisible, setIsPopupVisible } = props;
    const [alert, setAlert] = useState<string | null>(null);
    const [startWeek, setStartWeek] = useState<number>(1);
    const [scheduleLength, setScheduleLength] = useState<number>(1);
    const [isScheduling, setIsScheduling] = useState<boolean>(false);
    const [isIntervalVariable, setIsIntervalVariable] = useState<boolean>(false);
    const [isBalanced, setIsBalanced] = useState<boolean>(false);
    const { setCurrentSchedule } = useCleaningSchedulerContext();

    const newSchedule = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setIsScheduling(true)
        const result = await getNewSchedule(startWeek, scheduleLength, isIntervalVariable, isBalanced)
        setIsScheduling(false)

        if (isSchedule(result)) {
            setCurrentSchedule(result)
            setIsPopupVisible()
        } else {
            setAlert(`${result.statusCode} ${result.statusText}`);
        }
    }

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
                    "w-96",
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
                <h2 className="text-xl text-[#4393c9] font-semibold mb-3">Create New Schedule</h2>
                <form onSubmit={newSchedule}>
                    <div className="mb-4">
                        <label htmlFor="startWeek" className="block font-medium text-[#4393c9]">
                            Starting week of your new schedule:
                        </label>
                        <select
                            id="startWeek"
                            value={startWeek}
                            onChange={(e) => setStartWeek(Number(e.currentTarget.value))}
                            className="mt-1 p-2 border-[#4393c9] rounded-lg w-28 bg-[#4393c9]"
                            required>
                            <option value="">Select Week</option>
                            {Array.from({ length: 52 }, (_, index) => (
                                <option key={index + 1} value={index + 1}>
                                    Week {index + 1}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div className="mb-4">
                        <label htmlFor="scheduleLength" className="block font-medium text-[#4393c9]">
                            Choose how long your schedule should be:
                        </label>
                        <select
                            id="scheduleLength"
                            value={scheduleLength}
                            onChange={(e) => setScheduleLength(Number(e.currentTarget.value))}
                            className="mt-1 p-2 border-[#4393c9] rounded-lg w-52 bg-[#4393c9]"
                            required>
                            <option value="">Select Schedule Length</option>
                            {Array.from({ length: 10 }, (_, index) => (
                                <option key={index + 1} value={index + 1}>
                                    {index + 1} {index == 0 ? "week" : "weeks"}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div className="mb-4">
                        <label htmlFor="isIntervalVariable" className="block font-medium text-[#4393c9]">
                            Should the repeat interval be variable?
                            <div className="text-xs">
                                (This option will give a sub-optimal solution. Minimum repeat interval will be used otherwise.)
                            </div>
                        </label>
                        <input type="checkbox"
                            id="isIntervalVariable"
                            value={String(isIntervalVariable)}
                            checked={isIntervalVariable}
                            className="bg-[#4393c9] my-auto ml-2"
                            onChange={(e) => setIsIntervalVariable(e.currentTarget.checked)}
                        ></input>
                    </div>
                    <div className="mb-4">
                        <label htmlFor="isBalanced" className="block font-medium text-[#4393c9]">
                            Should the schedule balance the workload?
                            <div className="text-xs">
                                (This option will try to minimalize the standard deviation of the workload.)
                            </div>
                        </label>
                        <input type="checkbox"
                            id="isBalanced"
                            value={String(isBalanced)}
                            checked={isBalanced}
                            className="bg-[#4393c9] my-auto ml-2"
                            onChange={(e) => setIsBalanced(e.currentTarget.checked)}
                        ></input>
                    </div>
                    <div className="flex justify-center">
                        <button
                            type="submit"
                            className={classNames("w-full",
                                "bg-blue-600",
                                "text-[#0f2057]",
                                "py-2 px-4",
                                "rounded-lg",
                                "hover:bg-blue-700",
                                "focus:outline-none focus:ring-2 focus:ring-blue-600",
                                "disabled:hover:bg-blue-600")}
                            disabled={isScheduling}
                        >
                            {!isScheduling && <strong>Schedule</strong>}
                            {isScheduling &&
                                <div className='flex space-x-2 justify-center items-end'>
                                    <span className="font-bold">Scheduling</span>
                                    <div className='h-2 w-2 mb-1 bg-[#0f2057] rounded-full animate-bounce [animation-delay:-0.3s]'></div>
                                    <div className='h-2 w-2 mb-1 bg-[#0f2057] rounded-full animate-bounce [animation-delay:-0.15s]'></div>
                                    <div className='h-2 w-2 mb-1 bg-[#0f2057] rounded-full animate-bounce'></div>
                                </div>
                            }
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </Popup>
}