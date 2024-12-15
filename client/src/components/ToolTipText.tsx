import classNames from "classnames"


export const ToolTipText = () => {

    return (
        <div
            className={classNames(
                "invisible",
                "text-xs",
                "bg-[#f0f7dabe]",
                "text-center",
                "rounded-md",
                "absolute",
                "z-[1]",
                "p-1",
                "bottom-[70%]",
                "opacity-0",
                "transition-opacity",
                "duration-200",
                "ease-in-out"
            ) + " toolTipText"}
        >
            This feature will come soon!
        </div>

    )

}