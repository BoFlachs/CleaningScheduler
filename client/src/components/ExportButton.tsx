import classNames from "classnames"

type Props = {
    exportFunction: () => void;
    text: string;
}

export const ExportButton = (props: Props) => {
    const { exportFunction, text } = props;

    return <button className={classNames(
        "rounded-full",
        "p-2",
        "mt-5",
        "mb-3",
        {"ml-3": text == ".pdf"},
        "float-left",
        "bg-green-500/40",
        "hover:bg-green-500",
    )}
        onClick={exportFunction}
    > 
    Export as {text}
    </button>

}