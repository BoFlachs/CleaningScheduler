import { createContext, useContext, useState } from "react";
import { Person, Schedule, Task } from "../types";

type ContextType = {
    currentSchedule: Schedule | undefined;
    setCurrentSchedule: (schedule: Schedule | undefined) => void;
    currentPerson: Person | undefined;
    setCurrentPerson: (person: Person | undefined) => void;
    currentTask: Task | undefined;
    setCurrentTask: (task: Task | undefined) => void;
}

const CleaningSchedulerContext = createContext<ContextType>({
    currentSchedule: undefined,
    setCurrentSchedule() {},
    currentPerson: undefined,
    setCurrentPerson() {},
    currentTask: undefined,
    setCurrentTask() {}
});

type Props = React.PropsWithChildren;

export const CleaningSchedulerProvider = (props: Props) => {
    const { children } = props;
    const [ currentSchedule, setCurrentSchedule ] = useState<Schedule | undefined>(undefined)
    const [ currentPerson, setCurrentPerson ] = useState<Person | undefined>(undefined)
    const [ currentTask, setCurrentTask ] = useState<Task | undefined>(undefined)


    return <CleaningSchedulerContext.Provider value={{
        currentSchedule: currentSchedule,
        setCurrentSchedule: setCurrentSchedule,
        currentPerson: currentPerson,
        setCurrentPerson: setCurrentPerson,
        currentTask: currentTask,
        setCurrentTask: setCurrentTask,
    }}>{children}</CleaningSchedulerContext.Provider>
}

export const useCleaningSchedulerContext = () => {
    const context = useContext(CleaningSchedulerContext);

    if (context === undefined) {
        throw new Error('useCleaningSchedulerContext must be used within a CleaningSchedulerProvider');
    }

    return context;
}