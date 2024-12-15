import classNames from "classnames"

type Props = {
    text: string,
    changeFunction: () => void;
}

export const ChangeButton = (props: Props) => {
    const { text, changeFunction } = props;

    return <button className={classNames(
        "rounded-full",
        "p-2",
        "mt-5",
        "mb-3",
        "mx-5",
        "text-[#0f2057] ",
        "bg-green-500/40",
        "hover:bg-green-800",
    )}
        onClick={changeFunction}
    >{text}</button>
}