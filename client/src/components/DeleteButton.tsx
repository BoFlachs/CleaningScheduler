import classNames from "classnames"

type Props = {
    text: string,
    deleteFunction: () => void;
}

export const DeleteButton = (props: Props) => {
    const {text, deleteFunction} = props;

    return <button className={classNames(
        "bg-[#A84545]",
        "rounded-full",
        "p-2",
        "mt-5",
        "mb-3",
        "mx-5",
        "float-right",
        "hover:bg-[#682b2b]",
    )}
        onClick={deleteFunction}
    >{text}</button>
}