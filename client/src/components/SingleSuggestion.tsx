import classNames from "classnames"

type Props = {
    taskInList: (taskName: string) => boolean | undefined
    addSuggestedTask: (taskName: string,
        costs: number,
        lastDoneAt: number,
        isRepeated: boolean,
        minRepeat: number,
        maxRepeat: number) => Promise<void>
    taskName: string,
    costs: number,
    lastDoneAt: number,
    isRepeated: boolean,
    minRepeat: number,
    maxRepeat: number
}


export const SingleSuggestion = (props: Props) => {
    const { taskInList, addSuggestedTask, 
        taskName, costs, lastDoneAt, isRepeated,
    minRepeat, maxRepeat } = props;

    return <>
        {!taskInList(taskName) &&
            <button
                className={classNames(
                    "border-2 rounded-full",
                    "w-48",
                    "p-2",
                    "text-[#0f2057] ",
                    "border-[#0f2057]",
                    "bg-[#4393c9]",
                    "hover:text-[#4393c9] hover:bg-[#0f2057]",
                    "hover:border-[#0f2057] duration-300",
                )}
                onClick={() => addSuggestedTask(taskName, costs, lastDoneAt, isRepeated, minRepeat, maxRepeat)}>
                {taskName}
            </button>
        }
    </>
}