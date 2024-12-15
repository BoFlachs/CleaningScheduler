import { useEffect, useState } from "react";
import * as types from "../types";
import { exportScheduleAsPDF, exportScheduleAsTxt, getSchedule } from "../services/apiGet";
import moment from "moment";
import classNames from "classnames";
import "../style/schedule.css";
import { deleteSchedule } from "../services/apiDelete";
import { useCleaningSchedulerContext } from "../contexts/CleaningSchedulerContext";
import { DeleteButton } from "./DeleteButton";
import { ExportButton } from "./ExportButton";

type Props = {
    createdAtString: string
}

export const Schedule = (props: Props) => {
    var { createdAtString } = props;
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [schedule, setSchedule] = useState<types.Schedule | undefined>(undefined)
    const { setCurrentSchedule } = useCleaningSchedulerContext();

    useEffect(() => {
        const fetchSchedule = async () => {
            try {
                setLoading(true)
                const schedule = await getSchedule(createdAtString)
                setSchedule(schedule)
            } catch (error) {
                setError("Failed to load the data")
            } finally {
                setLoading(false)
            }
        };
        fetchSchedule();
    }, [createdAtString]);

    const deleteCurrentSchedule = async () => {
        const result = await deleteSchedule(createdAtString)

        if (result == 204) {
            const fakeSchedule: types.Schedule = { createdAt: Date.now() as unknown as Date, weekList: [], score: 0 }
            setCurrentSchedule(fakeSchedule);
            setSchedule(undefined);
        } else {
            console.log("Something unforeseen went wrong...")
        }
    }

    const exportCurrentScheduleAsTxt = async () => {
        const result = await exportScheduleAsTxt(createdAtString)

        if (result != "") {
            const blob = new Blob([result], { type: 'text/plain' });

            const url = URL.createObjectURL(blob);

            const a = document.createElement('a');
            a.href = url;
            a.download = `schedule_${createdAtString}.txt`;
            document.body.appendChild(a);
            a.click();

            URL.revokeObjectURL(url);
            document.body.removeChild(a);

        } else {
            console.log("Failed to get the schedel as .txt..")
        }
    }

    const exportCurrentScheduleAsPdf = async () => {
        const response = await exportScheduleAsPDF(createdAtString)


        if(response == undefined){
           console.log("Something went wrong") 
        } else{
            const pdf = await response.blob()
            const blob = new Blob([pdf], { type: 'application/pdf' });

            const url = URL.createObjectURL(blob);

            const a = document.createElement('a');
            a.href = url;
            a.download = `schedule_${createdAtString}.pdf`;
            document.body.appendChild(a);
            a.click();

            URL.revokeObjectURL(url);
            document.body.removeChild(a);
           
        }
    }

    if (loading) return <p>Loading</p>
    if (error) return <p>{error}</p>

    return <div className="w-full">
        {schedule ? (
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
                <h1 className="font-semibold mb-4 text-xl">
                    Schedule Created At: <br />
                    <div className="text-base">
                        {moment.utc(schedule.createdAt).format("DD-MM-YYYY HH:mm:ss")}
                    </div></h1>
                <h2 className="font-semibold mb-1">Schedule:</h2>
                <p>Score: {schedule.score}</p>
                <div className="scrollable-container">
                    <table className={classNames("border border-collapse",
                        "border-[#082150]",
                        "divide-y divide-black",
                    )}>
                        <thead>
                            <tr>
                                <th></th>
                                <th className="border border-[#082150] px-2 py-2">Task</th>
                                <th className="border border-[#082150] px-2 py-2">Person</th>
                                <th className="border border-[#082150] px-2 py-2">Costs (minutes)</th>
                            </tr>
                        </thead>
                        <tbody>
                            {Array.from(schedule.weekList.entries()).flatMap(([index, week]) => [
                                <tr key={`week-${index}`} className="border border-[#082150]">
                                    <td className="border border-[#082150] px-2 py-2">Week {week.weekNumber}</td>
                                    <td className="border border-[#082150] px-2 py-2"></td>
                                    <td className="border border-[#082150] px-2 py-2"></td>
                                    <td className="border border-[#082150] px-2 py-2"></td>
                                </tr>,
                                ...Array.from(week.taskAssignment.entries()).map(([task, person], taskIndex) => (
                                    <tr key={`task-${index}-${taskIndex}`} className="border border-[#082150]">
                                        <td className="border border-[#082150] px-2 py-2"></td>
                                        <td className="border border-[#082150] px-2 py-2">{task.name}</td>
                                        <td className="border border-[#082150] px-2 py-2">{person.name}</td>
                                        <td className="border border-[#082150] px-2 py-2">{task.costs} Minutes</td>
                                    </tr>
                                )),
                            ])}
                        </tbody>
                    </table>
                    <ExportButton text={".txt"} exportFunction={exportCurrentScheduleAsTxt} />
                    <ExportButton text={".pdf"} exportFunction={exportCurrentScheduleAsPdf} />
                    <DeleteButton text={"Delete Schedule"} deleteFunction={deleteCurrentSchedule} />
                </div>
            </div>
        ) : (
            <p className="p-10">No schedule found.</p>
        )}
    </div>
}