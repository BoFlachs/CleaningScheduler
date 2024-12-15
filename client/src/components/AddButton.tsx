import classNames from "classnames"

type Props = {
    text: string,
    addFunction: () => void;
}

export const AddButton = (props: Props) => {
    const { text, addFunction } = props;

    return <button className={classNames(
        "border-2 rounded-full",
        "w-48",
        "p-2",
        "text-[#0f2057] ",
        "border-[#0f2057]",
        "bg-green-500/40",
        "hover:bg-green-500",
        "hover:border-[#0f2057] duration-300",
    )}
        onClick={addFunction}
    >{text}</button>
}