import classNames from "classnames";
import { ToolTipText } from "./ToolTipText";
import "../style/tooltip.css";

type Props = {
    text: string,
    onScheduleButtonClick: () => void,
    isDisabled: boolean
}

export const ScheduleButton = (props: Props) => {
    const { text, onScheduleButtonClick, isDisabled} = props;

    return <>
        <button className={classNames(
            "w-[90%] h-[60px] rounded-full text-l border-4",
            "font-semibold",
            "px-4",
            "my-2",
            "relative",
            {"hover:text-[#4393c9] ": !isDisabled},
            {"hover:bg-[#082150] duration-300": !isDisabled},
            "text-[#0f2057] bg-[#4393c9] border-[#082150]",
            {"cursor-not-allowed bg-gray-100/30" :isDisabled},
            {"feature-coming-soon": isDisabled }
        )}
            onClick={onScheduleButtonClick}
            disabled={isDisabled}
        >
            {isDisabled && <ToolTipText />}
            {text}
        </button>
    </>
}