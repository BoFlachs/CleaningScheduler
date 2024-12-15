import classNames from "classnames";
import { To, useNavigate } from "react-router-dom";

type Props = {
    to: To;
    text: string;
};

export const NavButton = (props: Props) => {
    const { to, text } = props;
    const navigate = useNavigate();

    return (<button className={classNames(
        "w-[300px] h-[50px] rounded-full text-xl border-4",
        "my-6",
        "text-[#4393c9] ", 
        "border-[#4393c9]",
        "bg-[#082150] ", 
        "hover:text-[#0f2057] hover:bg-[#4393c9]",
        "hover:border-[#082150] duration-300"
    )}
        onClick={() => navigate(to)}>
        {text}
    </button>)
}
