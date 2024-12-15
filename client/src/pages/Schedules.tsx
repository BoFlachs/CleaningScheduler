import classNames from "classnames"
import { Schedule } from "../components/Schedule"
import { ScheduleButton } from "../components/ScheduleButton"
import { SideNav } from "../components/SideNav"
import { Key, useEffect, useState } from "react"
import * as types from "../types"
import { getAllSchedules } from "../services/apiGet"
import moment from "moment"
import { useCleaningSchedulerContext } from "../contexts/CleaningSchedulerContext"
import { SchedulePrompt } from "../components/SchedulePrompt"
import mop from "../assets/mop.png"

export const Schedules = () => {
    const [loading, setLoading] = useState<boolean>(true);
    const [, setError] = useState<string | null>(null);
    const [schedules, setSchedules] = useState<types.Schedule[] | undefined>(undefined)
    const [currentScheduleString, setCurrentScheduleString] = useState<string | undefined>(undefined)
    const { currentSchedule } = useCleaningSchedulerContext()
    const [isPopupVisible, setIsPopupVisible] = useState(false);

    const togglePopup = () => {
        setIsPopupVisible((prev) => !prev);
    };

    useEffect(() => {
        const fetchSchedules = async () => {
            try {
                setLoading(true)
                const schedules = await getAllSchedules()
                setSchedules(schedules)
                if (schedules?.length != 0) {
                    setCurrentScheduleString(schedules![0].createdAt.toString())
                }
            } catch (error) {
                setError("Failed to load the data")
            } finally {
                setLoading(false)
            }
        };
        fetchSchedules();
    }, [currentSchedule]);

    const showSchedule = (createdAtString: string) => {
        if (schedules != undefined) {
            var selectedSchedule = schedules[0];
            for (const schedule of schedules) {
                if (schedule.createdAt.toString() == createdAtString) {
                    selectedSchedule = schedule
                }
            }
            setCurrentScheduleString(selectedSchedule.createdAt.toString())
        }
    }

    return <>
        <aside className={classNames(
            "col-start-1",
            "bg-[#244788]",
            "flex",
            "flex-col",
            "pt-6",
            "items-center"
        )}>
            <div className={classNames(
                "h-[40px]",
                "text-2xl"
            )}>
                Schedule</div>
            <ScheduleButton onScheduleButtonClick={togglePopup} text="New schedule" isDisabled={false}/>
            <ScheduleButton onScheduleButtonClick={() => { }} text="Reschedule with locked tasks" isDisabled={true}/>
            <SideNav includeSchedules={false} />
        </aside>
        <main className={classNames(
            "col-start-2",
            "bg-[#4393c9]",
            "flex",
            "justify-center",
        )}>
            {currentScheduleString == undefined && <p className="m-4 mt-10">No schedule to show</p>}
            {currentScheduleString != undefined && <Schedule createdAtString={currentScheduleString} />}
            {isPopupVisible && <SchedulePrompt isPopupVisible={isPopupVisible} setIsPopupVisible={togglePopup} />}
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
                    "text-[#082150]"
                )
            }>
                <p className="font-bold text-lg ">
                    Schedule History</p>
                <p className="text-sm mb-2">See the schedule created at:</p>
                <div className={classNames(
                    "text-sm",
                    "space-y-2",
                    "scrollable-container"
                )}>
                    {loading && <p>Schedules loading</p>}
                    {!loading && schedules?.length == 0 && <p>No schedules found</p>}
                    {schedules != undefined && schedules.map(schedule => (
                        <button
                            key={schedule.createdAt.toString() as Key}
                            className={classNames(
                                "border-2 rounded-full",
                                "p-2",
                                "text-[#0f2057] ",
                                "border-[#0f2057]",
                                "bg-[#4393c9]",
                                { "!bg-[#0f2057]": currentScheduleString == schedule.createdAt.toString() },
                                { "text-[#4393c9]": currentScheduleString == schedule.createdAt.toString() },
                                "hover:text-[#4393c9] hover:bg-[#0f2057]",
                                "hover:border-[#0f2057] duration-300",
                            )}
                            disabled={currentScheduleString == schedule.createdAt.toString()}
                            onClick={() => showSchedule(schedule.createdAt.toString())}
                        >
                            {moment.utc(schedule.createdAt).format("DD-MM-YYYY HH:mm:ss")}
                        </button>
                    ))}
                </div>
            </div>
        </aside>
    </>
}